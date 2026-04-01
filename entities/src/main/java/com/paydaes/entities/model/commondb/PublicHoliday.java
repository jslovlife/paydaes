package com.paydaes.entities.model.commondb;

import com.paydaes.entities.model.tms.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// public holiday calendar per client — shared across all companies under same client
// year column denormalized for easy "get all holidays in 2025" queries
@Entity
@Table(
    name = "public_holidays",
    uniqueConstraints = @UniqueConstraint(name = "uq_public_holiday_date", columnNames = "holiday_date")
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PublicHoliday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String name;

    // derived from holidayDate but stored for cheaper filtering
    @Column(nullable = false)
    private int year;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
