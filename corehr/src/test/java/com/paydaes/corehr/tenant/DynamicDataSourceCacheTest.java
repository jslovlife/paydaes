package com.paydaes.corehr.tenant;

import com.paydaes.corehr.client.TmsServiceClient;
import com.paydaes.corehr.config.TenantPoolProperties;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// use mockConstruction to build dummy hikar ds so no real MySQL needed.
@ExtendWith(MockitoExtension.class)
class DynamicDataSourceCacheTest {

    @Mock 
    TmsServiceClient tmsServiceClient;

    @Mock 
    TenantSchemaInitializer schemaInitializer;

    @Mock 
    CommonDbSchemaInitializer commonDbSchemaInitializer;

    TenantPoolProperties poolProperties;
    DynamicDataSourceCache cache;

    @BeforeEach
    void setup() {
        poolProperties = new TenantPoolProperties(); 
        cache = new DynamicDataSourceCache(tmsServiceClient, schemaInitializer, commonDbSchemaInitializer, poolProperties);
        TenantContext.clear();
    }

    @Test
    void getOrCreate_cacheMiss_fetchesFromTms() {
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("db1", 3307, "alpha_db"));

        try (MockedConstruction<HikariDataSource> ignored = mockConstruction(HikariDataSource.class)) {
            cache.getOrCreate(DataSourceKey.forCompany(1L));
        }

        verify(tmsServiceClient).getCompanyDbConnection(1L);
    }


    @Test
    void getOrCreate_cacheHit_doesNotCallTmsAgain() {
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("db1", 3307, "alpha_db"));

        try (MockedConstruction<HikariDataSource> ignored = mockConstruction(HikariDataSource.class)) {
            cache.getOrCreate(DataSourceKey.forCompany(1L));
            cache.getOrCreate(DataSourceKey.forCompany(1L));
            cache.getOrCreate(DataSourceKey.forCompany(1L));
        }

        verify(tmsServiceClient, times(1)).getCompanyDbConnection(1L);
    }

    @Test
    void evict_forcesRefreshOnNextAccess() {
        DataSourceKey key = DataSourceKey.forCompany(1L);
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("db1", 3307, "alpha_db"));

        try (MockedConstruction<HikariDataSource> ignored = mockConstruction(HikariDataSource.class)) {
            cache.getOrCreate(key);
            cache.evict(key);
            cache.getOrCreate(key);
        }

        verify(tmsServiceClient, times(2)).getCompanyDbConnection(1L);
    }

    @Test
    void differentCompanies_getIndependentDatasources() {
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("db-host", 3307, "company1_db"));
        when(tmsServiceClient.getCompanyDbConnection(2L)).thenReturn(makeConnDto("db-host", 3307, "company2_db"));

        DataSource ds1, ds2;
        try (MockedConstruction<HikariDataSource> mocked = mockConstruction(HikariDataSource.class)) {
            ds1 = cache.getOrCreate(DataSourceKey.forCompany(1L));
            ds2 = cache.getOrCreate(DataSourceKey.forCompany(2L));
        }

        // company should have own ds
        assertThat(ds1).isNotSameAs(ds2);
        verify(tmsServiceClient).getCompanyDbConnection(1L);
        verify(tmsServiceClient).getCompanyDbConnection(2L);
    }

    @Test
    void companyDbAndCommonDb_getIndependentDatasources() {
        TenantContext.setCurrentTenant(10L, 1L);
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("db-host", 3307, "alpha_company_db"));
        when(tmsServiceClient.getClientCommonDbConnection(10L)).thenReturn(makeConnDto("db-host", 3306, "alpha_common_db"));

        DataSource companyDs, commonDs;
        try (MockedConstruction<HikariDataSource> mocked = mockConstruction(HikariDataSource.class)) {
            companyDs = cache.getOrCreate(DataSourceKey.forCompany(1L));
            commonDs  = cache.getOrCreate(DataSourceKey.forCommonDb(10L));
        }

        assertThat(companyDs).isNotSameAs(commonDs);
    }

    @Test
    void companyDatasourceLoad_triggersTenantSchemaInitializer() {
        TenantContext.setCurrentTenant(10L, 1L);
        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(makeConnDto("h", 3307, "db"));
        when(tmsServiceClient.getClientCommonDbConnection(10L)).thenThrow(new RuntimeException("not configured"));

        try (MockedConstruction<HikariDataSource> ignored = mockConstruction(HikariDataSource.class)) {
            cache.getOrCreate(DataSourceKey.forCompany(1L));
        }

        verify(schemaInitializer).initialize(any());
        verify(commonDbSchemaInitializer, never()).initialize(any());
    }

    @Test
    void commonDbDatasourceLoad_triggersCommonDbSchemaInitializer() {
        when(tmsServiceClient.getClientCommonDbConnection(10L)).thenReturn(makeConnDto("h", 3306, "common_db"));

        try (MockedConstruction<HikariDataSource> ignored = mockConstruction(HikariDataSource.class)) {
            cache.getOrCreate(DataSourceKey.forCommonDb(10L));
        }

        verify(commonDbSchemaInitializer).initialize(any());
        verify(schemaInitializer, never()).initialize(any());
    }

    private DbConnectionDto makeConnDto(String host, int port, String db) {
        return new DbConnectionDto(1L, true, host, port, db, "user", "pass", null, null);
    }
}
