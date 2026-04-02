package com.paydaes.tms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.entities.dto.tms.ClientDto;
import com.paydaes.tms.config.JpaConfig;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.ClientService;
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
        controllers = ClientController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class ClientControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ClientService clientService;

    private ClientDto sampleClient() {
        return new ClientDto(1L, "ABC Corp", "admin@abc.com", "+60111234567", null, null);
    }

    @Test
    void createClient_returnsCreated() throws Exception {
        ClientDto input    = new ClientDto(null, "ABC Corp", "admin@abc.com", "+60111234567", null, null);
        ClientDto response = sampleClient();
        when(clientService.createClient(any())).thenReturn(response);

        mockMvc.perform(post("/api/tms/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ABC Corp"));
    }

    @Test
    void createClient_duplicateEmail_returnsConflict() throws Exception {
        when(clientService.createClient(any()))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/api/tms/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleClient())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void getClientById_found_returnsOk() throws Exception {
        when(clientService.getClientById(1L)).thenReturn(Optional.of(sampleClient()));

        mockMvc.perform(get("/api/tms/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@abc.com"));
    }

    @Test
    void getClientById_notFound_returns404() throws Exception {
        when(clientService.getClientById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tms/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClients_returnsOk() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of(sampleClient()));

        mockMvc.perform(get("/api/tms/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("ABC Corp"));
    }

    @Test
    void getAllClients_emptyList_returnsOk() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of());

        mockMvc.perform(get("/api/tms/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchClients_returnsMatchingResults() throws Exception {
        when(clientService.searchClientsByName("ABC")).thenReturn(List.of(sampleClient()));

        mockMvc.perform(get("/api/tms/clients/search").param("name", "ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ABC Corp"));
    }

    @Test
    void getClientByEmail_found_returnsOk() throws Exception {
        when(clientService.getClientByEmail("admin@abc.com")).thenReturn(Optional.of(sampleClient()));

        mockMvc.perform(get("/api/tms/clients/email/admin@abc.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@abc.com"));
    }

    @Test
    void getClientByEmail_notFound_returns404() throws Exception {
        when(clientService.getClientByEmail("unknown@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tms/clients/email/unknown@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient_returnsOk() throws Exception {
        ClientDto updated = new ClientDto(1L, "ABC Corp Updated", "admin@abc.com", "+60111111111", null, null);
        when(clientService.updateClient(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/tms/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC Corp Updated"));
    }

    @Test
    void updateClient_notFound_returns404() throws Exception {
        when(clientService.updateClient(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Client not found: 99"));

        mockMvc.perform(put("/api/tms/clients/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleClient())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_returnsNoContent() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/tms/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(1L);
    }

    @Test
    void deleteClient_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Client not found: 99"))
                .when(clientService).deleteClient(99L);

        mockMvc.perform(delete("/api/tms/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTotalClientCount_returnsOk() throws Exception {
        when(clientService.getTotalClientCount()).thenReturn(5L);

        mockMvc.perform(get("/api/tms/clients/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
