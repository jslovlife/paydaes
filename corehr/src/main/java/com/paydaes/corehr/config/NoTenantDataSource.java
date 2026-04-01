package com.paydaes.corehr.config;

import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.SQLException;

// dummy datasource - if somehow getConnection() being called without tenant context, throw this
public class NoTenantDataSource extends AbstractDataSource {

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException(
            "No tenant context is set. All CoreHR endpoints required X-Client-Id and X-Company-Id request headers.");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }
}
