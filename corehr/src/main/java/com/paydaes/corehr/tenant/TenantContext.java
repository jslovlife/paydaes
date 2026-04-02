package com.paydaes.corehr.tenant;

// store tenant info per thread - spring mvc is thread-per-request so this should works fine
public final class TenantContext {

    private static final ThreadLocal<Long> CLIENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> COMPANY_ID = new ThreadLocal<>();
    private static final ThreadLocal<DataSourceKey> DS_KEY = new ThreadLocal<>();

    private TenantContext() {}

    // called by TenantFilter on every request, default to company db
    public static void setCurrentTenant(Long clientId, Long companyId) {
        CLIENT_ID.set(clientId);
        COMPANY_ID.set(companyId);
        DS_KEY.set(DataSourceKey.forCompany(companyId));
    }

    public static Long getCurrentClientId() { 
        return CLIENT_ID.get(); 
    }
    public static Long getCurrentCompanyId() { 
        return COMPANY_ID.get(); 
    }
    public static DataSourceKey getCurrentDataSourceKey() { 
        return DS_KEY.get(); 
    }

    public static void useCompanyDb() {
        Long companyId = COMPANY_ID.get();
        if (companyId != null) {
            DS_KEY.set(DataSourceKey.forCompany(companyId));
        }
    }

    public static void useCommonDb() {
        Long clientId = CLIENT_ID.get();
        if (clientId != null) {
            DS_KEY.set(DataSourceKey.forCommonDb(clientId));
        }
    }

    public static boolean hasTenant() {
        return COMPANY_ID.get() != null;
    }

    // need to call this in finally block, else thread pool will carry over old tenant info
    public static void clear() {
        CLIENT_ID.remove();
        COMPANY_ID.remove();
        DS_KEY.remove();
    }
}
