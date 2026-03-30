package com.paydaes.tms.service.impl;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dto.tms.CompanyDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyDao companyDao;
    private final ClientDao clientDao;

    @Override
    public CompanyDto createCompany(Long clientId, CompanyDto dto) {
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

        if (companyDao.existsByNameAndClientId(dto.getName(), clientId)) {
            throw new DuplicateResourceException(
                "Company '" + dto.getName() + "' already exists for client " + clientId);
        }

        return toDto(companyDao.save(new Company(dto.getName(), client)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyDto> getCompanyById(Long id) {
        return companyDao.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyDao.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getCompaniesByClientId(Long clientId) {
        return companyDao.findByClientId(clientId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CompanyDto updateCompany(Long id, CompanyDto dto) {
        Company company = companyDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        company.setName(dto.getName());
        return toDto(companyDao.save(company));
    }

    @Override
    public void deleteCompany(Long id) {
        companyDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        companyDao.deleteById(id);
    }

    private CompanyDto toDto(Company c) {
        return new CompanyDto(
                c.getId(),
                c.getName(),
                c.getClient().getId(),
                c.getClient().getName(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
