package com.paydaes.entities.repository.commondb;

import com.paydaes.entities.model.commondb.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

    List<PublicHoliday> findByYearOrderByHolidayDateAsc(int year);

    List<PublicHoliday> findByYearAndIsActiveOrderByHolidayDateAsc(int year, boolean isActive);

    Optional<PublicHoliday> findByHolidayDate(LocalDate holidayDate);

    boolean existsByHolidayDate(LocalDate holidayDate);
}
