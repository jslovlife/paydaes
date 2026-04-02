package com.paydaes.entities.dao.commondb;

import com.paydaes.entities.model.commondb.PublicHoliday;
import com.paydaes.entities.repository.commondb.PublicHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PublicHolidayDao {

    private final PublicHolidayRepository publicHolidayRepository;

    public PublicHoliday save(PublicHoliday holiday)                         { return publicHolidayRepository.save(holiday); }

    public Optional<PublicHoliday> findById(Long id)                         { return publicHolidayRepository.findById(id); }

    public Optional<PublicHoliday> findByDate(LocalDate date)                { return publicHolidayRepository.findByHolidayDate(date); }

    public List<PublicHoliday> findByYear(int year)                          { return publicHolidayRepository.findByYearOrderByHolidayDateAsc(year); }

    public List<PublicHoliday> findActiveByYear(int year)                    { return publicHolidayRepository.findByYearAndIsActiveOrderByHolidayDateAsc(year, true); }

    public boolean existsByDate(LocalDate date)                              { return publicHolidayRepository.existsByHolidayDate(date); }

    public void deleteById(Long id)                                          { publicHolidayRepository.deleteById(id); }
}
