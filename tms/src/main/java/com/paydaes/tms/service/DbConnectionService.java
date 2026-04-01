package com.paydaes.tms.service;

import com.paydaes.entities.dto.tms.DbConnectionDto;

import java.util.List;

public interface DbConnectionService {

    record SaveResult<T>(T data, boolean created) {}

    // -- client commondb --

    SaveResult<DbConnectionDto> saveClientDbConnection(Long clientId, DbConnectionDto dto);

    DbConnectionDto getClientDbConnection(Long clientId);

    List<DbConnectionDto> getAllClientDbConnections();

    DbConnectionDto toggleClientDbConnection(Long clientId);

    void deleteClientDbConnection(Long clientId);

    // Company specific db

    SaveResult<DbConnectionDto> saveCompanyDbConnection(Long companyId, DbConnectionDto dto);

    DbConnectionDto getCompanyDbConnection(Long companyId);

    List<DbConnectionDto> getCompanyDbConnectionsByClientId(Long clientId);

    DbConnectionDto toggleCompanyDbConnection(Long companyId);

    void deleteCompanyDbConnection(Long companyId);
}
