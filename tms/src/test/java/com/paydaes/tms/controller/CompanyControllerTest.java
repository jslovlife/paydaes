package com.paydaes.tms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.entities.dto.tms.CompanyDto;
import com.paydaes.tms.config.JpaConfig;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CompanyController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class CompanyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  CompanyService companyService;

    private CompanyDto sampleCompany() {
        return new CompanyDto(1L, "ABC HQ", 1L, "ABC Corp", null, null);
    }

    @Test
    void createCompany_returnsCreated() throws Exception {
        CompanyDto input = new CompanyDto(null, "ABC HQ", null, null, null, null);
        when(companyService.createCompany(eq(1L), any())).thenReturn(sampleCompany());

        mockMvc.perform(post("/api/tms/companies/client/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ABC HQ"))
                .andExpect(jsonPath("$.clientId").value(1));
    }

    @Test
    void createCompany_clientNotFound_returns404() throws Exception {
        when(companyService.createCompany(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Client not found: 99"));

        mockMvc.perform(post("/api/tms/companies/client/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCompany())))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCompany_duplicateName_returnsConflict() throws Exception {
        when(companyService.createCompany(eq(1L), any()))
                .thenThrow(new DuplicateResourceException("Company name already exists under this client"));

        mockMvc.perform(post("/api/tms/companies/client/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCompany())))
                .andExpect(status().isConflict());
    }

    @Test
    void getCompanyById_found_returnsOk() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(Optional.of(sampleCompany()));

        mockMvc.perform(get("/api/tms/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC HQ"))
                .andExpect(jsonPath("$.clientName").value("ABC Corp"));
    }

    @Test
    void getCompanyById_notFound_returns404() throws Exception {
        when(companyService.getCompanyById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tms/companies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCompanies_returnsOk() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany()));

        mockMvc.perform(get("/api/tms/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("ABC HQ"));
    }

    @Test
    void getCompaniesByClient_returnsOk() throws Exception {
        CompanyDto second = new CompanyDto(2L, "BCD HQ", 1L, "ABC Corp", null, null);
        when(companyService.getCompaniesByClientId(1L)).thenReturn(List.of(sampleCompany(), second));

        mockMvc.perform(get("/api/tms/companies/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getCompaniesByClient_emptyList_returnsOk() throws Exception {
        when(companyService.getCompaniesByClientId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tms/companies/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateCompany_returnsOk() throws Exception {
        CompanyDto updated = new CompanyDto(1L, "ABC HQ Renamed", 1L, "ABC Corp", null, null);
        when(companyService.updateCompany(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/tms/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC HQ Renamed"));
    }

    @Test
    void updateCompany_notFound_returns404() throws Exception {
        when(companyService.updateCompany(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Company not found: 99"));

        mockMvc.perform(put("/api/tms/companies/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCompany())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCompany_returnsNoContent() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/tms/companies/1"))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(1L);
    }

    @Test
    void deleteCompany_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Company not found: 99"))
                .when(companyService).deleteCompany(99L);

        mockMvc.perform(delete("/api/tms/companies/99"))
                .andExpect(status().isNotFound());
    }
}
