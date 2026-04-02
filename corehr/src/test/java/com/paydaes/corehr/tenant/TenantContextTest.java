package com.paydaes.corehr.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void setCurrentTenant_storesClientAndCompanyId() {
        TenantContext.setCurrentTenant(10L, 5L);

        assertThat(TenantContext.getCurrentClientId()).isEqualTo(10L);
        assertThat(TenantContext.getCurrentCompanyId()).isEqualTo(5L);
    }

    @Test
    void setCurrentTenant_defaultsToCompanyDsKey() {
        TenantContext.setCurrentTenant(10L, 5L);

        DataSourceKey key = TenantContext.getCurrentDataSourceKey();
        assertThat(key.getType()).isEqualTo(DataSourceKey.Type.COMPANY);
        assertThat(key.getId()).isEqualTo(5L);
    }

    @Test
    void useCommonDb_switchesDsKeyToClientCommonDb() {
        TenantContext.setCurrentTenant(10L, 5L);
        TenantContext.useCommonDb();

        DataSourceKey key = TenantContext.getCurrentDataSourceKey();
        assertThat(key.getType()).isEqualTo(DataSourceKey.Type.COMMON_DB);
        assertThat(key.getId()).isEqualTo(10L);
    }

    @Test
    void useCompanyDb_switchesDsKeyBackToCompanyDb() {
        TenantContext.setCurrentTenant(10L, 5L);
        TenantContext.useCommonDb();
        TenantContext.useCompanyDb();

        DataSourceKey key = TenantContext.getCurrentDataSourceKey();
        assertThat(key.getType()).isEqualTo(DataSourceKey.Type.COMPANY);
        assertThat(key.getId()).isEqualTo(5L);
    }

    @Test
    void useCommonDb_withNoTenantSet_doesNotThrow() {
        TenantContext.useCommonDb();
        assertThat(TenantContext.getCurrentDataSourceKey()).isNull();
    }

    @Test
    void clear_removesAllContextValues() {
        TenantContext.setCurrentTenant(10L, 5L);
        TenantContext.clear();

        assertThat(TenantContext.getCurrentClientId()).isNull();
        assertThat(TenantContext.getCurrentCompanyId()).isNull();
        assertThat(TenantContext.getCurrentDataSourceKey()).isNull();
        assertThat(TenantContext.hasTenant()).isFalse();
    }

    @Test
    void hasTenant_returnsTrueWhenTenantIsSet() {
        TenantContext.setCurrentTenant(10L, 5L);
        assertThat(TenantContext.hasTenant()).isTrue();
    }

    @Test
    void hasTenant_returnsFalseWhenContextIsEmpty() {
        assertThat(TenantContext.hasTenant()).isFalse();
    }

    @Test
    void differentThreads_holdIndependentTenantContexts() throws InterruptedException {
        TenantContext.setCurrentTenant(1L, 1L);

        AtomicReference<Long> threadBClientId  = new AtomicReference<>();
        AtomicReference<Long> threadBCompanyId = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Thread threadB = new Thread(() -> {
            TenantContext.setCurrentTenant(2L, 2L);
            threadBClientId.set(TenantContext.getCurrentClientId());
            threadBCompanyId.set(TenantContext.getCurrentCompanyId());
            TenantContext.clear();
            done.countDown();
        });
        threadB.start();
        done.await();

        assertThat(TenantContext.getCurrentClientId()).isEqualTo(1L);
        assertThat(TenantContext.getCurrentCompanyId()).isEqualTo(1L);

        assertThat(threadBClientId.get()).isEqualTo(2L);
        assertThat(threadBCompanyId.get()).isEqualTo(2L);
    }

    @Test
    void differentCompaniesUnderSameClient_produceDifferentDsKeys() {
        TenantContext.setCurrentTenant(1L, 10L);
        DataSourceKey keyForCompany10 = TenantContext.getCurrentDataSourceKey();

        TenantContext.setCurrentTenant(1L, 11L);
        DataSourceKey keyForCompany11 = TenantContext.getCurrentDataSourceKey();

        assertThat(keyForCompany10).isNotEqualTo(keyForCompany11);
        assertThat(keyForCompany10.toString()).isEqualTo("COMPANY:10");
        assertThat(keyForCompany11.toString()).isEqualTo("COMPANY:11");
    }

    @Test
    void companyKeyAndCommonDbKey_areNeverEqual() {
        DataSourceKey companyKey  = DataSourceKey.forCompany(1L);
        DataSourceKey commonDbKey = DataSourceKey.forCommonDb(1L);

        assertThat(companyKey).isNotEqualTo(commonDbKey);
        assertThat(companyKey.toString()).isEqualTo("COMPANY:1");
        assertThat(commonDbKey.toString()).isEqualTo("COMMON_DB:1");
    }
}
