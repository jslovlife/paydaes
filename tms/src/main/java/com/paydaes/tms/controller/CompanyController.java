package com.paydaes.tms.controller;

import com.paydaes.entities.dto.tms.CompanyDto;
import com.paydaes.tms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tms/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/client/{clientId}")
    public ResponseEntity<CompanyDto> createCompany(@PathVariable Long clientId,
                                                    @RequestBody CompanyDto dto) {
        return new ResponseEntity<>(companyService.createCompany(clientId, dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CompanyDto>> getCompaniesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(companyService.getCompaniesByClientId(clientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id,
                                                    @RequestBody CompanyDto dto) {
        return ResponseEntity.ok(companyService.updateCompany(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
