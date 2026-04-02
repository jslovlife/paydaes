package com.paydaes.corehr.tenant;

import java.util.Objects;

// key to identify which db to connect - type + owner id
// COMPANY  = company specific db  (owner id is companyId)
// COMMON_DB = client shared db    (owner id is clientId)
// toString() used as cache key, e.g. "COMPANY:5" or "COMMON_DB:2"
public final class DataSourceKey {

    public enum Type { COMPANY, COMMON_DB }

    private final Type type;
    private final Long id;

    private DataSourceKey(Type type, Long id) {
        this.type = type;
        this.id = id;
    }

    public static DataSourceKey forCompany(Long companyId) {
        return new DataSourceKey(Type.COMPANY, companyId);
    }

    public static DataSourceKey forCommonDb(Long clientId) {
        return new DataSourceKey(Type.COMMON_DB, clientId);
    }

    public Type getType() { return type; }
    public Long getId()   { return id; }

    @Override
    public String toString() { return type + ":" + id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceKey k)) return false;
        return type == k.type && Objects.equals(id, k.id);
    }

    @Override
    public int hashCode() { return Objects.hash(type, id); }
}
