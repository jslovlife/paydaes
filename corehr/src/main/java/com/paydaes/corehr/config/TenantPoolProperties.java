package com.paydaes.corehr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "corehr.tenant.pool")
public class TenantPoolProperties {

    private int maximumPoolSize = 5;

    // 0 = shrink to zero when no traffic, save connection for idle tenants
    // dont set this higher unless you know what you doing
    private int minimumIdle = 0;

    private long connectionTimeoutMs = 30_000;

    // close idle connection after 10min, must be less than mysql wait timeout (default 8h)
    private long idleTimeoutMs = 600_000;

    // ping idle connection every 2min so it doesnt go stale after db restart
    private long keepaliveTimeMs = 120_000;

    // how long we reuse the cached pool before refresh from TMS, 30min default
    private long cacheTtlSeconds = 1_800;
}
