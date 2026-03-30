package com.paydaes.entities.dao.tms;

import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.repository.tms.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyDao {

    private final CompanyRepository companyRepository;

    public Company save(Company company) { return companyRepository.save(company); }

    public Optional<Company> findById(Long id) { return companyRepository.findById(id); }

    public List<Company> findAll() { return companyRepository.findAll(); }

    public List<Company> findByClientId(Long clientId) { return companyRepository.findByClientId(clientId); }

    public boolean existsByNameAndClientId(String name, Long clientId) { return companyRepository.existsByNameAndClientId(name, clientId); }

    public void deleteById(Long id) { companyRepository.deleteById(id); }
}
