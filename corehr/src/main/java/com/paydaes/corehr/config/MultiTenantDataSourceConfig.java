package com.paydaes.corehr.config;

import com.paydaes.corehr.tenant.DynamicDataSourceCache;
import com.paydaes.corehr.tenant.DynamicDataSourceRouter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class MultiTenantDataSourceConfig {

    // fallback when no tenant (By right, should not hit this)
    public DataSource noTenantDataSource() {
        return new NoTenantDataSource();
    }

    // the actual datasource - routes to correct tenant db based on request headers
    // actual db pools created on the fly, no pre-registration needed
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("noTenantDataSource") DataSource noTenant,
                                  DynamicDataSourceCache dynamicDataSourceCache) {
        DynamicDataSourceRouter router = new DynamicDataSourceRouter(dynamicDataSourceCache);
        router.setDefaultDataSource(noTenant);
        router.setTargetDataSources(Map.of());
        router.afterPropertiesSet();
        return router;
    }
}
