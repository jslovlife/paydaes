package com.paydaes.entities.dto.corehr;

import com.paydaes.entities.model.corehr.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private String jobTitle;
    private String department;
    private BigDecimal salary;
    private Employee.EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
