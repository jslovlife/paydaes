package com.paydaes.tms.service.impl;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.ClientDbConnectionDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dao.tms.CompanyDbConnectionDao;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.ClientDbConnection;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.DbConnectionService;
import com.paydaes.tms.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DbConnectionServiceImpl implements DbConnectionService {

    private final ClientDbConnectionDao clientDbConnectionDao;
    private final CompanyDbConnectionDao companyDbConnectionDao;
    private final ClientDao clientDao;
    private final CompanyDao companyDao;
    private final AesEncryptionUtil aesEncryptionUtil;

    @Value("${tms.keystore.key-alias:v1}")
    private String currentKeyVersion;

    // -- client commondb --

    @Override
    public SaveResult<DbConnectionDto> saveClientDbConnection(Long clientId, DbConnectionDto dto) {
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

        ClientDbConnection conn = clientDbConnectionDao.findByClientId(clientId)
                .orElse(new ClientDbConnection());
        boolean isNew = conn.getId() == null;

        conn.setClient(client);
        conn.setActive(true);
        conn.setHost(dto.getHost());
        conn.setPort(dto.getPort());
        conn.setDatabaseName(dto.getDatabaseName());
        conn.setUsername(aesEncryptionUtil.encrypt(dto.getUsername()));
        conn.setPassword(aesEncryptionUtil.encrypt(dto.getPassword()));
        conn.setKeyVersion(currentKeyVersion);

        return new SaveResult<>(toDto(clientDbConnectionDao.save(conn)), isNew);
    }

    @Override
    @Transactional(readOnly = true)
    public DbConnectionDto getClientDbConnection(Long clientId) {
        ClientDbConnection conn = clientDbConnectionDao.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No commondb connection found for client: " + clientId));
        if (!conn.isActive()) {
            throw new ResourceNotFoundException(
                    "Commondb connection is disabled for client: " + clientId);
        }
        return toDto(conn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbConnectionDto> getAllClientDbConnections() {
        return clientDbConnectionDao.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DbConnectionDto toggleClientDbConnection(Long clientId) {
        ClientDbConnection conn = clientDbConnectionDao.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No commondb connection found for client: " + clientId));
        conn.setActive(!conn.isActive());
        return toDto(clientDbConnectionDao.save(conn));
    }

    @Override
    public void deleteClientDbConnection(Long clientId) {
        ClientDbConnection conn = clientDbConnectionDao.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No commondb connection found for client: " + clientId));
        clientDbConnectionDao.deleteById(conn.getId());
    }

    // Company specific db

    @Override
    public SaveResult<DbConnectionDto> saveCompanyDbConnection(Long companyId, DbConnectionDto dto) {
        Company company = companyDao.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        CompanyDbConnection conn = companyDbConnectionDao.findByCompanyId(companyId)
                .orElse(new CompanyDbConnection());
        boolean isNew = conn.getId() == null;

        conn.setCompany(company);
        conn.setActive(true);
        conn.setHost(dto.getHost());
        conn.setPort(dto.getPort());
        conn.setDatabaseName(dto.getDatabaseName());
        conn.setUsername(aesEncryptionUtil.encrypt(dto.getUsername()));
        conn.setPassword(aesEncryptionUtil.encrypt(dto.getPassword()));
        conn.setKeyVersion(currentKeyVersion);

        return new SaveResult<>(toDto(companyDbConnectionDao.save(conn)), isNew);
    }

    @Override
    @Transactional(readOnly = true)
    public DbConnectionDto getCompanyDbConnection(Long companyId) {
        CompanyDbConnection conn = companyDbConnectionDao.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No db connection found for company: " + companyId));
        if (!conn.isActive()) {
            throw new ResourceNotFoundException(
                    "Company db connection is disabled for company: " + companyId);
        }
        return toDto(conn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbConnectionDto> getCompanyDbConnectionsByClientId(Long clientId) {
        return companyDao.findByClientId(clientId).stream()
                .flatMap(c -> companyDbConnectionDao.findByCompanyId(c.getId()).stream())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DbConnectionDto toggleCompanyDbConnection(Long companyId) {
        CompanyDbConnection conn = companyDbConnectionDao.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No db connection found for company: " + companyId));
        conn.setActive(!conn.isActive());
        return toDto(companyDbConnectionDao.save(conn));
    }

    @Override
    public void deleteCompanyDbConnection(Long companyId) {
        CompanyDbConnection conn = companyDbConnectionDao.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No db connection found for company: " + companyId));
        companyDbConnectionDao.deleteById(conn.getId());
    }

    // helpers - reuse for both client and company

    private DbConnectionDto toDto(ClientDbConnection c) {
        return new DbConnectionDto(
                c.getId(),
                c.isActive(),
                c.getHost(),
                c.getPort(),
                c.getDatabaseName(),
                aesEncryptionUtil.decrypt(c.getUsername()),
                aesEncryptionUtil.decrypt(c.getPassword()),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private DbConnectionDto toDto(CompanyDbConnection c) {
        return new DbConnectionDto(
                c.getId(),
                c.isActive(),
                c.getHost(),
                c.getPort(),
                c.getDatabaseName(),
                aesEncryptionUtil.decrypt(c.getUsername()),
                aesEncryptionUtil.decrypt(c.getPassword()),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
