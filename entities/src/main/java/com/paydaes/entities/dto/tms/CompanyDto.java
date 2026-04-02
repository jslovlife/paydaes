package com.paydaes.entities.dto.tms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String name;
    private Long clientId;
    private String clientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
