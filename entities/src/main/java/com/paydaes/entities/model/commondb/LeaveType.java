package com.paydaes.entities.model.commondb;

import com.paydaes.entities.model.tms.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// leave type config shared across all companies under the same client
// e.g. Annual Leave, Medical Leave, Maternity — set once at client level
@Entity
@Table(name = "leave_types")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // short code for the leave type, unique per client db
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // null means unlimited — useful for sick leave in some companies
    @Column(name = "max_days_per_year", precision = 5, scale = 1)
    private BigDecimal maxDaysPerYear;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = true;

    // how many unused days can carry forward to next year
    @Column(name = "carry_forward_days", nullable = false)
    private int carryForwardDays = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
