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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    @Override
    public SaveResult<DbConnectionDto> saveClientDbConnection(Long clientId, DbConnectionDto dto) {
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

        Optional<ClientDbConnection> existing = clientDbConnectionDao.findByClientId(clientId);
        boolean isNew = existing.isEmpty();

        ClientDbConnection conn = existing.orElse(new ClientDbConnection());
        conn.setClient(client);
        conn.setHost(dto.getHost());
        conn.setPort(dto.getPort());
        conn.setDatabaseName(dto.getDatabaseName());
        conn.setUsername(aesEncryptionUtil.encrypt(dto.getUsername()));
        conn.setPassword(aesEncryptionUtil.encrypt(dto.getPassword()));

        return new SaveResult<>(toDto(clientDbConnectionDao.save(conn)), isNew);
    }

    @Override
    @Transactional(readOnly = true)
    public DbConnectionDto getClientDbConnection(Long clientId) {
        return clientDbConnectionDao.findByClientId(clientId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No commondb connection found for client: " + clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbConnectionDto> getAllClientDbConnections() {
        return clientDbConnectionDao.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteClientDbConnection(Long clientId) {
        ClientDbConnection conn = clientDbConnectionDao.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No commondb connection found for client: " + clientId));
        clientDbConnectionDao.deleteById(conn.getId());
    }

    @Override
    public SaveResult<DbConnectionDto> saveCompanyDbConnection(Long companyId, DbConnectionDto dto) {
        Company company = companyDao.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        Optional<CompanyDbConnection> existing = companyDbConnectionDao.findByCompanyId(companyId);
        boolean isNew = existing.isEmpty();

        CompanyDbConnection conn = existing.orElse(new CompanyDbConnection());
        conn.setCompany(company);
        conn.setHost(dto.getHost());
        conn.setPort(dto.getPort());
        conn.setDatabaseName(dto.getDatabaseName());
        conn.setUsername(aesEncryptionUtil.encrypt(dto.getUsername()));
        conn.setPassword(aesEncryptionUtil.encrypt(dto.getPassword()));

        return new SaveResult<>(toDto(companyDbConnectionDao.save(conn)), isNew);
    }

    @Override
    @Transactional(readOnly = true)
    public DbConnectionDto getCompanyDbConnection(Long companyId) {
        return companyDbConnectionDao.findByCompanyId(companyId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No db connection found for company: " + companyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbConnectionDto> getCompanyDbConnectionsByClientId(Long clientId) {
        List<Company> companies = companyDao.findByClientId(clientId);
        return companies.stream()
                .map(c -> companyDbConnectionDao.findByCompanyId(c.getId()))
                .filter(Optional::isPresent)
                .map(opt -> toDto(opt.get()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCompanyDbConnection(Long companyId) {
        CompanyDbConnection conn = companyDbConnectionDao.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No db connection found for company: " + companyId));
        companyDbConnectionDao.deleteById(conn.getId());
    }

    private DbConnectionDto toDto(ClientDbConnection c) {
        return new DbConnectionDto(
                c.getId(),
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
