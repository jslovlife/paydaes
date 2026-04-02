package com.paydaes.entities.dto.commondb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal maxDaysPerYear;
    private Boolean isPaid;
    private Integer carryForwardDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
