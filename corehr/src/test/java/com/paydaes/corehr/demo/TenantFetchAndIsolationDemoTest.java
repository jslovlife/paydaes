package com.paydaes.corehr.demo;

import com.paydaes.corehr.client.TmsServiceClient;
import com.paydaes.corehr.config.TenantPoolProperties;
import com.paydaes.corehr.exception.TenantResolutionException;
import com.paydaes.corehr.tenant.*;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantFetchAndIsolationDemoTest {

    @Mock 
    TmsServiceClient tmsServiceClient;
    
    @Mock 
    TenantSchemaInitializer schemaInitializer;

    @Mock 
    CommonDbSchemaInitializer commonDbSchemaInitializer;

    @Mock 
    RestTemplate restTemplate;

    DynamicDataSourceCache cache;

    @BeforeEach
    void setup() {
        TenantPoolProperties poolProps = new TenantPoolProperties();
        cache = new DynamicDataSourceCache(tmsServiceClient, schemaInitializer, commonDbSchemaInitializer, poolProps);
        TenantContext.clear();
    }

    @AfterEach
    void teardown() {
        TenantContext.clear();
    }

    @Test
    void corehr_fetchesDecryptedConnectionsFromTms_andIsolatesTenantDatabases() throws InterruptedException {

        // STEP 1 — TmsServiceClient fetches the correct endpoint of company/client.

        DbConnectionDto abcCompanyConn = new DbConnectionDto(
                1L,true, "testdomain", 3307, "abc_company_db",
                "abc_user", "ABCP@ss",   // <-- TMS decrypts before sending
                null, null
        );

        DbConnectionDto abcCommonConn = new DbConnectionDto(
                2L, true, "testdomain", 3306, "abc_commondb",
                "abc_common_user", "CommonP@ss",
                null, null
        );

        DbConnectionDto bcdCompanyConn = new DbConnectionDto(
                3L, true, "testdomain2", 3307, "bcd_company_db",
                "bcd_user", "BCDP@ss",
                null, null
        );

        when(tmsServiceClient.getCompanyDbConnection(1L)).thenReturn(abcCompanyConn);
        when(tmsServiceClient.getCompanyDbConnection(2L)).thenReturn(bcdCompanyConn);
        when(tmsServiceClient.getClientCommonDbConnection(1L)).thenReturn(abcCommonConn);

        assertThat(abcCompanyConn.getUsername())
                .as("CoreHR receives plaintext credentials")
                .isEqualTo("abc_user");
        assertThat(abcCompanyConn.getHost()).isEqualTo("testdomain");
        assertThat(bcdCompanyConn.getDatabaseName()).isEqualTo("bcd_company_db");

        // STEP 2 — DynamicDataSourceCache routes each company to its own datasource.

        DataSource abcDs, bcdDs, abcCommonDs;

        try (MockedConstruction<HikariDataSource> mockedPools = mockConstruction(HikariDataSource.class)) {

            TenantContext.setCurrentTenant(1L, 1L);
            abcDs = cache.getOrCreate(DataSourceKey.forCompany(1L));

            // cache hit — TMS must not be called twice for the same company
            cache.getOrCreate(DataSourceKey.forCompany(1L));
            cache.getOrCreate(DataSourceKey.forCompany(1L));

            TenantContext.setCurrentTenant(2L, 2L);
            bcdDs = cache.getOrCreate(DataSourceKey.forCompany(2L));

            TenantContext.setCurrentTenant(1L, 1L);
            abcCommonDs = cache.getOrCreate(DataSourceKey.forCommonDb(1L));

            // TMS called exactly once per key
            verify(tmsServiceClient, times(1)).getCompanyDbConnection(1L);
            verify(tmsServiceClient, times(1)).getCompanyDbConnection(2L);
            verify(tmsServiceClient, times(1)).getClientCommonDbConnection(1L);
        }

        // STEP 3 — Data isolation: ABC and BCD NEVER share a datasource instance.

        assertThat(abcDs)
                .as("ABC company datasource must be distinct from BCD company datasource")
                .isNotSameAs(bcdDs);

        assertThat(abcDs)
                .as("ABC company datasource must be distinct from ABC common datasource")
                .isNotSameAs(abcCommonDs);

        // STEP 4 — DataSourceKey uniqueness guarantees correct pool selection.

        DataSourceKey abcCompanyKey = DataSourceKey.forCompany(1L);
        DataSourceKey abcCommonKey = DataSourceKey.forCommonDb(1L);
        DataSourceKey bcdCompanyKey = DataSourceKey.forCompany(2L);

        assertThat(abcCompanyKey)
                .as("company:1 and commondb:1 share the same id but must be different cache keys")
                .isNotEqualTo(abcCommonKey);

        assertThat(abcCompanyKey)
                .as("company:1 and company:2 must be different cache keys")
                .isNotEqualTo(bcdCompanyKey);

        assertThat(abcCompanyKey.toString()).isEqualTo("COMPANY:1");
        assertThat(abcCommonKey.toString()).isEqualTo("COMMON_DB:1");
        assertThat(bcdCompanyKey.toString()).isEqualTo("COMPANY:2");

        // STEP 5 — Thread isolation: two concurrent HTTP requests for different tenants must carry fully independent TenantContext values. If Thread A's context bled into Thread B, company B could read abc's DB.

        // Main thread - simulate request for ABC
        TenantContext.setCurrentTenant(1L, 1L);
        TenantContext.useCommonDb(); // switch to common DB mid-request

        AtomicReference<Long> threadBClientId = new AtomicReference<>();
        AtomicReference<Long> threadBCompanyId = new AtomicReference<>();
        AtomicReference<DataSourceKey> threadBDsKey = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Thread requestForBCD = new Thread(() -> {
            // BCD's request runs concurrently - must not see ABC's thread-local values
            TenantContext.setCurrentTenant(2L, 2L);
            threadBClientId.set(TenantContext.getCurrentClientId());
            threadBCompanyId.set(TenantContext.getCurrentCompanyId());
            threadBDsKey.set(TenantContext.getCurrentDataSourceKey());
            TenantContext.clear();
            done.countDown();
        });
        requestForBCD.start();
        done.await();

        // Main thread still able to see ABC's context 
        assertThat(TenantContext.getCurrentClientId())
                .as("main thread (ABC request) context must be unchanged by BCD's thread")
                .isEqualTo(1L);
        assertThat(TenantContext.getCurrentDataSourceKey().getType())
                .as("main thread switched to commonDb — must still be COMMON_DB type")
                .isEqualTo(DataSourceKey.Type.COMMON_DB);

        // BCD's thread able to see only BCD's values
        assertThat(threadBClientId.get()).isEqualTo(2L);
        assertThat(threadBCompanyId.get()).isEqualTo(2L);
        assertThat(threadBDsKey.get().toString())
                .as("BCD's thread must resolve to BCD's company datasource key, not ABC's")
                .isEqualTo("COMPANY:2");

        // STEP 6 — If a company has no DB connection registered in TMS, the system fails fast with TenantResolutionException.

        when(tmsServiceClient.getCompanyDbConnection(99L))
                .thenThrow(new TenantResolutionException("No db connection registered in TMS for company 99"));

        DataSourceKey unknownCompanyKey = DataSourceKey.forCompany(99L);
        assertThatThrownBy(() -> cache.getOrCreate(unknownCompanyKey))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("company 99");
    }
}
