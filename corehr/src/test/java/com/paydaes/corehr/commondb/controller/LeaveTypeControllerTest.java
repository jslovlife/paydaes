package com.paydaes.corehr.commondb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.corehr.commondb.service.LeaveTypeService;
import com.paydaes.corehr.config.JpaConfig;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.entities.dto.commondb.LeaveTypeDto;
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
        controllers = LeaveTypeController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class LeaveTypeControllerTest {

    @Autowired 
    MockMvc mockMvc;

    @Autowired 
    ObjectMapper objectMapper;

    @MockBean 
    LeaveTypeService leaveTypeService;

    private MockHttpServletRequestBuilder withTenant(MockHttpServletRequestBuilder req) {
        return req.header("X-Client-Id", "1").header("X-Company-Id", "1");
    }

    private LeaveTypeDto sampleLeaveType() {
        return new LeaveTypeDto(1L, "ANNUAL", "Annual Leave", new BigDecimal("14.0"), true, 5, true, null, null);
    }

    @Test
    void createLeaveType_returnsCreated() throws Exception {
        LeaveTypeDto input = new LeaveTypeDto(null, "ANNUAL", "Annual Leave", new BigDecimal("14.0"), true, 5, true, null, null);
        when(leaveTypeService.createLeaveType(any())).thenReturn(sampleLeaveType());

        mockMvc.perform(withTenant(post("/api/corehr/leave-types"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ANNUAL"))
                .andExpect(jsonPath("$.maxDaysPerYear").value(14.0))
                .andExpect(jsonPath("$.carryForwardDays").value(5));
    }

    @Test
    void createLeaveType_duplicateCode_returnsConflict() throws Exception {
        when(leaveTypeService.createLeaveType(any()))
                .thenThrow(new DuplicateResourceException("Duplicated leave type code found: ANNUAL"));

        mockMvc.perform(withTenant(post("/api/corehr/leave-types"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleLeaveType())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicated leave type code found: ANNUAL"));
    }

    @Test
    void getLeaveTypeById_found_returnsOk() throws Exception {
        when(leaveTypeService.getLeaveTypeById(1L)).thenReturn(Optional.of(sampleLeaveType()));

        mockMvc.perform(withTenant(get("/api/corehr/leave-types/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ANNUAL"))
                .andExpect(jsonPath("$.paid").value(true));
    }

    @Test
    void getLeaveTypeById_notFound_returns404() throws Exception {
        when(leaveTypeService.getLeaveTypeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(withTenant(get("/api/corehr/leave-types/99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLeaveTypeByCode_found_returnsOk() throws Exception {
        when(leaveTypeService.getLeaveTypeByCode("ANNUAL")).thenReturn(Optional.of(sampleLeaveType()));

        mockMvc.perform(withTenant(get("/api/corehr/leave-types/code/ANNUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Annual Leave"));
    }

    @Test
    void getLeaveTypeByCode_notFound_returns404() throws Exception {
        when(leaveTypeService.getLeaveTypeByCode("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(withTenant(get("/api/corehr/leave-types/code/UNKNOWN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllLeaveTypes_returnsOk() throws Exception {
        LeaveTypeDto medical = new LeaveTypeDto(2L, "MEDICAL", "Medical Leave", new BigDecimal("14.0"), true, 0, true, null, null);
        when(leaveTypeService.getAllLeaveTypes()).thenReturn(List.of(sampleLeaveType(), medical));

        mockMvc.perform(withTenant(get("/api/corehr/leave-types")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].code").value("MEDICAL"));
    }

    @Test
    void getAllActiveLeaveTypes_returnsOnlyActive() throws Exception {
        when(leaveTypeService.getAllActiveLeaveTypes()).thenReturn(List.of(sampleLeaveType()));

        mockMvc.perform(withTenant(get("/api/corehr/leave-types/active")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void updateLeaveType_returnsOk() throws Exception {
        LeaveTypeDto updated = new LeaveTypeDto(1L, "ANNUAL", "Annual Leave", new BigDecimal("16.0"), true, 7, true, null, null);
        when(leaveTypeService.updateLeaveType(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(withTenant(put("/api/corehr/leave-types/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxDaysPerYear").value(16.0))
                .andExpect(jsonPath("$.carryForwardDays").value(7));
    }

    @Test
    void updateLeaveType_notFound_returns404() throws Exception {
        when(leaveTypeService.updateLeaveType(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Leave type not found: 99"));

        mockMvc.perform(withTenant(put("/api/corehr/leave-types/99"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleLeaveType())))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleLeaveType_returnsOk() throws Exception {
        LeaveTypeDto toggled = new LeaveTypeDto(1L, "ANNUAL", "Annual Leave", new BigDecimal("14.0"), true, 5, false, null, null);
        when(leaveTypeService.toggleLeaveType(1L)).thenReturn(toggled);

        mockMvc.perform(withTenant(patch("/api/corehr/leave-types/1/status")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteLeaveType_returnsNoContent() throws Exception {
        doNothing().when(leaveTypeService).deleteLeaveType(1L);

        mockMvc.perform(withTenant(delete("/api/corehr/leave-types/1")))
                .andExpect(status().isNoContent());

        verify(leaveTypeService).deleteLeaveType(1L);
    }

    @Test
    void deleteLeaveType_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Leave type not found for id: 99"))
                .when(leaveTypeService).deleteLeaveType(99L);

        mockMvc.perform(withTenant(delete("/api/corehr/leave-types/99")))
                .andExpect(status().isNotFound());
    }
}
