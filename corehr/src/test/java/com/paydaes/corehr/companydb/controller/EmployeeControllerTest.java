package com.paydaes.corehr.companydb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.corehr.companydb.service.EmployeeService;
import com.paydaes.corehr.config.JpaConfig;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.entities.dto.corehr.EmployeeDto;
import com.paydaes.entities.model.corehr.Employee.EmployeeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = EmployeeController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class EmployeeControllerTest {

    @Autowired 
    MockMvc mockMvc;

    @Autowired 
    ObjectMapper objectMapper;

    @MockBean  
    EmployeeService employeeService;

    private MockHttpServletRequestBuilder withTenant(MockHttpServletRequestBuilder req) {
        return req.header("X-Client-Id", "1").header("X-Company-Id", "1");
    }

    private EmployeeDto sampleEmployee() {
        return new EmployeeDto(1L, "EMP001", "Ali", "Hassan", "ali@abc.com",
                "+60111234567", null, "Software Engineer", "IT",
                new BigDecimal("5000.00"), EmployeeStatus.ACTIVE, null, null);
    }

    @Test
    void createEmployee_returnsCreated() throws Exception {
        EmployeeDto input = new EmployeeDto(null, "EMP001", "Ali", "Hassan", "ali@abc.com",
                null, null, null, null, null, null, null, null);
        when(employeeService.createEmployee(any())).thenReturn(sampleEmployee());

        mockMvc.perform(withTenant(post("/api/corehr/employees"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createEmployee_duplicateId_returnsConflict() throws Exception {
        when(employeeService.createEmployee(any()))
                .thenThrow(new DuplicateResourceException("Employee with ID already exists: EMP001"));

        mockMvc.perform(withTenant(post("/api/corehr/employees"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleEmployee())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Employee with ID already exists: EMP001"));
    }

    @Test
    void createEmployee_missingTenantHeaders_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/corehr/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleEmployee())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeById_found_returnsOk() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.department").value("IT"));
    }

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(withTenant(get("/api/corehr/employees/99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeByEmployeeId_found_returnsOk() throws Exception {
        when(employeeService.getEmployeeByEmployeeId("EMP001")).thenReturn(Optional.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees/employee-id/EMP001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ali@abc.com"));
    }

    @Test
    void getEmployeeByEmployeeId_notFound_returns404() throws Exception {
        when(employeeService.getEmployeeByEmployeeId("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(withTenant(get("/api/corehr/employees/employee-id/UNKNOWN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEmployees_returnsOk() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employeeId").value("EMP001"));
    }

    @Test
    void getEmployeesByDepartment_returnsOk() throws Exception {
        when(employeeService.getEmployeesByDepartment("IT")).thenReturn(List.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees/department/IT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("IT"));
    }

    @Test
    void getEmployeesByStatus_returnsOk() throws Exception {
        when(employeeService.getEmployeesByStatus(EmployeeStatus.ACTIVE)).thenReturn(List.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees/status/ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void searchEmployees_returnsOk() throws Exception {
        when(employeeService.searchEmployeesByName("Ali")).thenReturn(List.of(sampleEmployee()));

        mockMvc.perform(withTenant(get("/api/corehr/employees/search").param("name", "Ali")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ali"));
    }

    @Test
    void updateEmployee_returnsOk() throws Exception {
        EmployeeDto updated = sampleEmployee();
        updated.setJobTitle("Senior Engineer");
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(withTenant(put("/api/corehr/employees/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Engineer"));
    }

    @Test
    void updateEmployee_notFound_returns404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Employee not found: 99"));

        mockMvc.perform(withTenant(put("/api/corehr/employees/99"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleEmployee())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployeeStatus_returnsOk() throws Exception {
        EmployeeDto terminated = sampleEmployee();
        terminated.setStatus(EmployeeStatus.TERMINATED);
        when(employeeService.updateEmployeeStatus(1L, EmployeeStatus.TERMINATED)).thenReturn(terminated);

        mockMvc.perform(withTenant(patch("/api/corehr/employees/1/status"))
                .param("status", "TERMINATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    void deleteEmployee_returnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(withTenant(delete("/api/corehr/employees/1")))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found: 99"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(withTenant(delete("/api/corehr/employees/99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeCountByStatus_returnsOk() throws Exception {
        when(employeeService.getEmployeeCountByStatus(EmployeeStatus.ACTIVE)).thenReturn(10L);

        mockMvc.perform(withTenant(get("/api/corehr/employees/count/status/ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}
