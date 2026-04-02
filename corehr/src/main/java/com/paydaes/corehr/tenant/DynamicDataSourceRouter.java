package com.paydaes.corehr.tenant;

import com.paydaes.corehr.exception.TenantResolutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

@Slf4j
public class DynamicDataSourceRouter extends AbstractRoutingDataSource {

    private final DynamicDataSourceCache cache;
    private DataSource defaultDataSource;

    public DynamicDataSourceRouter(DynamicDataSourceCache cache) {
        this.cache = cache;
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        super.setDefaultTargetDataSource(defaultDataSource);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceKey key = TenantContext.getCurrentDataSourceKey();
        return key != null ? key.toString() : null;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSourceKey key = TenantContext.getCurrentDataSourceKey();
        if (key == null) {
            return defaultDataSource;
        }
        log.debug("Routing to datasource key={}", key);
        try {
            return cache.getOrCreate(key);
        } catch (TenantResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TenantResolutionException("Failed to resolve datasource for key=" + key, e);
        }
    }
}
