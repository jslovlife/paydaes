package com.paydaes.entities.model.corehr;

import com.paydaes.entities.model.tms.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "department")
    private String department;

    @Column(name = "salary")
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeStatus status;

    public Employee(String employeeId, String firstName, String lastName, String email) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = EmployeeStatus.ACTIVE;
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, TERMINATED
    }
}
