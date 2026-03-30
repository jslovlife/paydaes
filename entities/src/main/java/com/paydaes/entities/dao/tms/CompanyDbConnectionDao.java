package com.paydaes.entities.dao.tms;

import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.entities.repository.tms.CompanyDbConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyDbConnectionDao {

    private final CompanyDbConnectionRepository repository;

    public CompanyDbConnection save(CompanyDbConnection connection) { return repository.save(connection); }

    public Optional<CompanyDbConnection> findByCompanyId(Long companyId) { return repository.findByCompanyId(companyId); }

    public Optional<CompanyDbConnection> findById(Long id) { return repository.findById(id); }

    public boolean existsByCompanyId(Long companyId) { return repository.existsByCompanyId(companyId); }

    public void deleteById(Long id) { repository.deleteById(id); }
}
