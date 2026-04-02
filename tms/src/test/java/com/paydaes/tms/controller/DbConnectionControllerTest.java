package com.paydaes.tms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.paydaes.tms.config.JpaConfig;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.DbConnectionService;
import com.paydaes.tms.service.DbConnectionService.SaveResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DbConnectionController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class DbConnectionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  DbConnectionService dbConnectionService;

    private DbConnectionDto validConnDto() {
        return new DbConnectionDto(null, true, "localhost", 3307, "abc_common_db", "paydaes", "secret123", null, null);
    }

    private DbConnectionDto savedConnDto(Long id, String dbName) {
        return new DbConnectionDto(id, true, "localhost", 3307, dbName, "paydaes", "secret123", null, null);
    }

    @Test
    void saveClientDbConnection_newEntry_returnsCreated() throws Exception {
        DbConnectionDto saved = savedConnDto(1L, "abc_common_db");
        when(dbConnectionService.saveClientDbConnection(eq(1L), any()))
                .thenReturn(new SaveResult<>(saved, true));

        mockMvc.perform(put("/api/tms/connections/client/1/commondb")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validConnDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.databaseName").value("abc_common_db"));
    }

    @Test
    void saveClientDbConnection_updateExisting_returnsOk() throws Exception {
        DbConnectionDto saved = savedConnDto(1L, "abc_common_db");
        when(dbConnectionService.saveClientDbConnection(eq(1L), any()))
                .thenReturn(new SaveResult<>(saved, false));

        mockMvc.perform(put("/api/tms/connections/client/1/commondb")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validConnDto())))
                .andExpect(status().isOk());
    }

    @Test
    void saveClientDbConnection_validationFails_returnsBadRequest() throws Exception {
        DbConnectionDto invalid = new DbConnectionDto(null, false, "", null, "", "", "", null, null);

        mockMvc.perform(put("/api/tms/connections/client/1/commondb")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClientDbConnection_returnsOk() throws Exception {
        when(dbConnectionService.getClientDbConnection(1L)).thenReturn(savedConnDto(1L, "abc_common_db"));

        mockMvc.perform(get("/api/tms/connections/client/1/commondb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.databaseName").value("abc_common_db"));
    }

    @Test
    void getClientDbConnection_notFound_returns404() throws Exception {
        when(dbConnectionService.getClientDbConnection(99L))
                .thenThrow(new ResourceNotFoundException("No commondb connection found for client: 99"));

        mockMvc.perform(get("/api/tms/connections/client/99/commondb"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClientDbConnections_returnsOk() throws Exception {
        when(dbConnectionService.getAllClientDbConnections())
                .thenReturn(List.of(savedConnDto(1L, "abc_common_db"), savedConnDto(2L, "bcd_common_db")));

        mockMvc.perform(get("/api/tms/connections/client/commondb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void toggleClientDbConnection_returnsOk() throws Exception {
        DbConnectionDto toggled = savedConnDto(1L, "abc_common_db");
        toggled.setActive(false);
        when(dbConnectionService.toggleClientDbConnection(1L)).thenReturn(toggled);

        mockMvc.perform(patch("/api/tms/connections/client/1/commondb/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteClientDbConnection_returnsNoContent() throws Exception {
        doNothing().when(dbConnectionService).deleteClientDbConnection(1L);

        mockMvc.perform(delete("/api/tms/connections/client/1/commondb"))
                .andExpect(status().isNoContent());

        verify(dbConnectionService).deleteClientDbConnection(1L);
    }

    @Test
    void saveCompanyDbConnection_newEntry_returnsCreated() throws Exception {
        DbConnectionDto saved = savedConnDto(10L, "abc_hq_db");
        when(dbConnectionService.saveCompanyDbConnection(eq(1L), any()))
                .thenReturn(new SaveResult<>(saved, true));

        mockMvc.perform(put("/api/tms/connections/company/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validConnDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.databaseName").value("abc_hq_db"));
    }

    @Test
    void saveCompanyDbConnection_updateExisting_returnsOk() throws Exception {
        DbConnectionDto saved = savedConnDto(10L, "abc_hq_db");
        when(dbConnectionService.saveCompanyDbConnection(eq(1L), any()))
                .thenReturn(new SaveResult<>(saved, false));

        mockMvc.perform(put("/api/tms/connections/company/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validConnDto())))
                .andExpect(status().isOk());
    }

    @Test
    void getCompanyDbConnection_returnsOk() throws Exception {
        when(dbConnectionService.getCompanyDbConnection(1L)).thenReturn(savedConnDto(10L, "abc_hq_db"));

        mockMvc.perform(get("/api/tms/connections/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.databaseName").value("abc_hq_db"));
    }

    @Test
    void getCompanyDbConnection_notFound_returns404() throws Exception {
        when(dbConnectionService.getCompanyDbConnection(99L))
                .thenThrow(new ResourceNotFoundException("No db connection found for company: 99"));

        mockMvc.perform(get("/api/tms/connections/company/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompanyDbConnectionsByClient_returnsOk() throws Exception {
        when(dbConnectionService.getCompanyDbConnectionsByClientId(1L))
                .thenReturn(List.of(savedConnDto(10L, "abc_hq_db")));

        mockMvc.perform(get("/api/tms/connections/client/1/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void toggleCompanyDbConnection_returnsOk() throws Exception {
        DbConnectionDto toggled = savedConnDto(10L, "abc_hq_db");
        toggled.setActive(false);
        when(dbConnectionService.toggleCompanyDbConnection(1L)).thenReturn(toggled);

        mockMvc.perform(patch("/api/tms/connections/company/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteCompanyDbConnection_returnsNoContent() throws Exception {
        doNothing().when(dbConnectionService).deleteCompanyDbConnection(1L);

        mockMvc.perform(delete("/api/tms/connections/company/1"))
                .andExpect(status().isNoContent());

        verify(dbConnectionService).deleteCompanyDbConnection(1L);
    }
}
