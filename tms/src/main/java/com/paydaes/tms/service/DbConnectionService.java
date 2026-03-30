package com.paydaes.tms.service;

import com.paydaes.entities.dto.tms.DbConnectionDto;

import java.util.List;

public interface DbConnectionService {

    record SaveResult<T>(T data, boolean created) {}

    SaveResult<DbConnectionDto> saveClientDbConnection(Long clientId, DbConnectionDto dto);

    DbConnectionDto getClientDbConnection(Long clientId);

    List<DbConnectionDto> getAllClientDbConnections();

    void deleteClientDbConnection(Long clientId);

    SaveResult<DbConnectionDto> saveCompanyDbConnection(Long companyId, DbConnectionDto dto);

    DbConnectionDto getCompanyDbConnection(Long companyId);

    List<DbConnectionDto> getCompanyDbConnectionsByClientId(Long clientId);

    void deleteCompanyDbConnection(Long companyId);
}
