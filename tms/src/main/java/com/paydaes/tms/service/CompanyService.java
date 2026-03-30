package com.paydaes.tms.service;

import com.paydaes.entities.dto.tms.CompanyDto;

import java.util.List;
import java.util.Optional;

public interface CompanyService {

    CompanyDto createCompany(Long clientId, CompanyDto dto);

    Optional<CompanyDto> getCompanyById(Long id);

    List<CompanyDto> getAllCompanies();

    List<CompanyDto> getCompaniesByClientId(Long clientId);

    CompanyDto updateCompany(Long id, CompanyDto dto);

    void deleteCompany(Long id);
}
