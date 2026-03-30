package com.paydaes.entities.dto.tms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbConnectionDto {

    private Long id;

    @NotBlank(message = "Host must not be blank")
    private String host;

    @NotNull(message = "Port must not be null")
    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private Integer port;

    @NotBlank(message = "Database name must not be blank")
    private String databaseName;

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
