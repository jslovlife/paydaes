package com.paydaes.corehr.commondb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paydaes.corehr.commondb.service.PublicHolidayService;
import com.paydaes.corehr.config.JpaConfig;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.entities.dto.commondb.PublicHolidayDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PublicHolidayController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class)
)
class PublicHolidayControllerTest {

    @Autowired 
    MockMvc mockMvc;

    @Autowired 
    ObjectMapper objectMapper;
    
    @MockBean  
    PublicHolidayService publicHolidayService;

    private MockHttpServletRequestBuilder withTenant(MockHttpServletRequestBuilder req) {
        return req.header("X-Client-Id", "1").header("X-Company-Id", "1");
    }

    private PublicHolidayDto sampleHoliday() {
        return new PublicHolidayDto(1L, LocalDate.of(2026, 1, 1), "New Year", 2026, true, null, null);
    }

    private PublicHolidayDto ramadanHoliday() {
        return new PublicHolidayDto(2L, LocalDate.of(2026, 3, 31), "CNY", 2026, true, null, null);
    }

    @Test
    void addHoliday_returnsCreated() throws Exception {
        PublicHolidayDto input = new PublicHolidayDto(null, LocalDate.of(2026, 1, 1), "New Year", 0, true, null, null);
        when(publicHolidayService.addHoliday(any())).thenReturn(sampleHoliday());

        mockMvc.perform(withTenant(post("/api/corehr/public-holidays"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Year"))
                .andExpect(jsonPath("$.year").value(2026));
    }

    @Test
    void addHoliday_duplicateDate_returnsConflict() throws Exception {
        when(publicHolidayService.addHoliday(any()))
                .thenThrow(new DuplicateResourceException("Holiday already exists for date: 2026-01-01"));

        mockMvc.perform(withTenant(post("/api/corehr/public-holidays"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleHoliday())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Holiday already exists for date: 2026-01-01"));
    }

    @Test
    void getHolidayById_found_returnsOk() throws Exception {
        when(publicHolidayService.getHolidayById(1L)).thenReturn(Optional.of(sampleHoliday()));

        mockMvc.perform(withTenant(get("/api/corehr/public-holidays/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Year"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getHolidayById_notFound_returns404() throws Exception {
        when(publicHolidayService.getHolidayById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(withTenant(get("/api/corehr/public-holidays/99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHolidaysByYear_returnsOk() throws Exception {
        when(publicHolidayService.getHolidaysByYear(2026)).thenReturn(List.of(sampleHoliday(), ramadanHoliday()));

        mockMvc.perform(withTenant(get("/api/corehr/public-holidays/year/2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[1].name").value("CNY"));
    }

    @Test
    void getHolidaysByYear_noHolidays_returnsEmptyList() throws Exception {
        when(publicHolidayService.getHolidaysByYear(2030)).thenReturn(List.of());

        mockMvc.perform(withTenant(get("/api/corehr/public-holidays/year/2030")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getActiveHolidaysByYear_returnsOnlyActive() throws Exception {
        when(publicHolidayService.getActiveHolidaysByYear(2026)).thenReturn(List.of(sampleHoliday()));

        mockMvc.perform(withTenant(get("/api/corehr/public-holidays/year/2026/active")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void updateHoliday_returnsOk() throws Exception {
        PublicHolidayDto updated = new PublicHolidayDto(1L, LocalDate.of(2026, 1, 1), "New Year 2026", 2026, true, null, null);
        when(publicHolidayService.updateHoliday(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(withTenant(put("/api/corehr/public-holidays/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Year 2026"));
    }

    @Test
    void updateHoliday_notFound_returns404() throws Exception {
        when(publicHolidayService.updateHoliday(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Holiday not found: 99"));

        mockMvc.perform(withTenant(put("/api/corehr/public-holidays/99"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleHoliday())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHoliday_returnsNoContent() throws Exception {
        doNothing().when(publicHolidayService).deleteHoliday(1L);

        mockMvc.perform(withTenant(delete("/api/corehr/public-holidays/1")))
                .andExpect(status().isNoContent());

        verify(publicHolidayService).deleteHoliday(1L);
    }

    @Test
    void deleteHoliday_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Holiday not found: 99"))
                .when(publicHolidayService).deleteHoliday(99L);

        mockMvc.perform(withTenant(delete("/api/corehr/public-holidays/99")))
                .andExpect(status().isNotFound());
    }
}
