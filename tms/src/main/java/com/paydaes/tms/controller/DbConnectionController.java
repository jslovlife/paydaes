package com.paydaes.tms.controller;

import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.paydaes.tms.service.DbConnectionService;
import com.paydaes.tms.service.DbConnectionService.SaveResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tms/connections")
@RequiredArgsConstructor
public class DbConnectionController {

    private final DbConnectionService dbConnectionService;

    @PutMapping("/client/{clientId}/commondb")
    public ResponseEntity<DbConnectionDto> saveClientDbConnection(
            @PathVariable Long clientId,
            @Valid @RequestBody DbConnectionDto dto) {
        SaveResult<DbConnectionDto> result = dbConnectionService.saveClientDbConnection(clientId, dto);
        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.data());
    }

    @GetMapping("/client/{clientId}/commondb")
    public ResponseEntity<DbConnectionDto> getClientDbConnection(@PathVariable Long clientId) {
        return ResponseEntity.ok(dbConnectionService.getClientDbConnection(clientId));
    }

    @GetMapping("/client/commondb")
    public ResponseEntity<List<DbConnectionDto>> getAllClientDbConnections() {
        return ResponseEntity.ok(dbConnectionService.getAllClientDbConnections());
    }

    @DeleteMapping("/client/{clientId}/commondb")
    public ResponseEntity<Void> deleteClientDbConnection(@PathVariable Long clientId) {
        dbConnectionService.deleteClientDbConnection(clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/company/{companyId}")
    public ResponseEntity<DbConnectionDto> saveCompanyDbConnection(
            @PathVariable Long companyId,
            @Valid @RequestBody DbConnectionDto dto) {
        SaveResult<DbConnectionDto> result = dbConnectionService.saveCompanyDbConnection(companyId, dto);
        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.data());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<DbConnectionDto> getCompanyDbConnection(@PathVariable Long companyId) {
        return ResponseEntity.ok(dbConnectionService.getCompanyDbConnection(companyId));
    }

    @GetMapping("/client/{clientId}/companies")
    public ResponseEntity<List<DbConnectionDto>> getCompanyDbConnectionsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(dbConnectionService.getCompanyDbConnectionsByClientId(clientId));
    }

    @DeleteMapping("/company/{companyId}")
    public ResponseEntity<Void> deleteCompanyDbConnection(@PathVariable Long companyId) {
        dbConnectionService.deleteCompanyDbConnection(companyId);
        return ResponseEntity.noContent().build();
    }
}
