package com.paydaes.corehr.commondb.service;

import com.paydaes.entities.dto.commondb.PublicHolidayDto;

import java.util.List;
import java.util.Optional;

public interface PublicHolidayService {

    PublicHolidayDto addHoliday(PublicHolidayDto dto);

    Optional<PublicHolidayDto> getHolidayById(Long id);

    List<PublicHolidayDto> getHolidaysByYear(int year);

    List<PublicHolidayDto> getActiveHolidaysByYear(int year);

    PublicHolidayDto updateHoliday(Long id, PublicHolidayDto dto);

    void deleteHoliday(Long id);
}
