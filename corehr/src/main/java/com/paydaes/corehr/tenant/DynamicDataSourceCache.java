package com.paydaes.corehr.tenant;

import com.paydaes.corehr.client.TmsServiceClient;
import com.paydaes.corehr.config.TenantPoolProperties;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

// cache the hikari pool per tenant so we dont recreate it every request
// first request for new tenant will be slow (call TMS + open connections), subsequent ones fast
// TTL set to 30min by default - after expire, next request will refresh from TMS
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicDataSourceCache {

    // cached entry - keep the datasource + when it was created
    private record CachedEntry(DataSource dataSource, long createdAtMs) {
        boolean isExpired(long ttlSeconds) {
            return System.currentTimeMillis() - createdAtMs > ttlSeconds * 1_000L;
        }
    }

    private final ConcurrentHashMap<String, CachedEntry> cache    = new ConcurrentHashMap<>();
    // per-key lock so tenant A reload wont block tenant B, only same key will wait
    private final ConcurrentHashMap<String, Object>      keyLocks = new ConcurrentHashMap<>();

    private final TmsServiceClient          tmsServiceClient;
    private final TenantSchemaInitializer   schemaInitializer;
    private final CommonDbSchemaInitializer commonDbSchemaInitializer;
    private final TenantPoolProperties      poolProperties;

    public DataSource getOrCreate(DataSourceKey key) {
        CachedEntry entry = cache.get(key.toString());
        if (entry != null && !entry.isExpired(poolProperties.getCacheTtlSeconds())) {
            return entry.dataSource();
        }
        return reload(key);
    }

    // manual evict - useful when connection details updated in TMS and dont want to wait for TTL
    public void evict(DataSourceKey key) {
        synchronized (lockFor(key)) {
            CachedEntry removed = cache.remove(key.toString());
            if (removed != null) {
                closeQuietly(removed.dataSource(), key);
                log.info("Evicted datasource for key={}", key);
            }
        }
    }

    private DataSource reload(DataSourceKey key) {
        synchronized (lockFor(key)) {
            // double check - another thread might have loaded already while we waiting
            CachedEntry entry = cache.get(key.toString());
            if (entry != null && !entry.isExpired(poolProperties.getCacheTtlSeconds())) {
                return entry.dataSource();
            }
            if (entry != null) {
                log.info("TTL expired for key={}, reload from TMS", key);
                closeQuietly(entry.dataSource(), key);
            }
            DataSource ds = load(key);
            cache.put(key.toString(), new CachedEntry(ds, System.currentTimeMillis()));
            return ds;
        }
    }

    private Object lockFor(DataSourceKey key) {
        return keyLocks.computeIfAbsent(key.toString(), k -> new Object());
    }

    private DataSource load(DataSourceKey key) {
        log.info("No pool for key={}, fetching from TMS and creating", key);
        DbConnectionDto dto = fetchFromTms(key);
        DataSource ds = buildDataSource(dto, key);
        if (key.getType() == DataSourceKey.Type.COMPANY) {
            schemaInitializer.initialize(ds);
            // first time seeing this company — proactively init common db for the client too
            // so both schemas are ready at the same time, no cold start on first commondb request
            warmUpCommonDb(TenantContext.getCurrentClientId());
        } else if (key.getType() == DataSourceKey.Type.COMMON_DB) {
            commonDbSchemaInitializer.initialize(ds);
        }
        log.info("Pool ready for key={} (db={})", key, dto.getDatabaseName());
        return ds;
    }

    // best effort — dont blow up company db setup if common db not configured yet
    private void warmUpCommonDb(Long clientId) {
        if (clientId == null) return;
        DataSourceKey commonKey = DataSourceKey.forCommonDb(clientId);
        if (cache.containsKey(commonKey.toString())) return;
        try {
            log.info("Warming up common db for clientId={}", clientId);
            getOrCreate(commonKey);
        } catch (Exception e) {
            log.warn("Common db not ready for clientId={} (maybe not configured yet): {}", clientId, e.getMessage());
        }
    }

    private DbConnectionDto fetchFromTms(DataSourceKey key) {
        return switch (key.getType()) {
            case COMPANY   -> tmsServiceClient.getCompanyDbConnection(key.getId());
            case COMMON_DB -> tmsServiceClient.getClientCommonDbConnection(key.getId());
        };
    }

    private DataSource buildDataSource(DbConnectionDto dto, DataSourceKey key) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort()
                + "/" + dto.getDatabaseName()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(poolProperties.getMaximumPoolSize());
        config.setMinimumIdle(poolProperties.getMinimumIdle());
        config.setConnectionTimeout(poolProperties.getConnectionTimeoutMs());
        config.setIdleTimeout(poolProperties.getIdleTimeoutMs());
        config.setKeepaliveTime(poolProperties.getKeepaliveTimeMs());
        config.setPoolName("corehr-" + key.getType().name().toLowerCase().replace('_', '-')
                + "-" + dto.getDatabaseName());
        return new HikariDataSource(config);
    }

    private void closeQuietly(DataSource ds, DataSourceKey key) {
        if (ds instanceof HikariDataSource h) {
            try {
                h.close();
            } catch (Exception e) {
                log.warn("Problem closing pool for key={}: {}", key, e.getMessage());
            }
        }
    }
}
