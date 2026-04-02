package com.paydaes.corehr.commondb.service.impl;

import com.paydaes.corehr.commondb.mapper.PublicHolidayMapper;
import com.paydaes.corehr.commondb.service.PublicHolidayService;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.corehr.tenant.annotation.UseCommonDb;
import com.paydaes.entities.dao.commondb.PublicHolidayDao;
import com.paydaes.entities.dto.commondb.PublicHolidayDto;
import com.paydaes.entities.model.commondb.PublicHoliday;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UseCommonDb
@Service
@Transactional
@RequiredArgsConstructor
public class PublicHolidayServiceImpl implements PublicHolidayService {

    private final PublicHolidayDao publicHolidayDao;
    private final PublicHolidayMapper mapper;

    @Override
    public PublicHolidayDto addHoliday(PublicHolidayDto dto) {
        if (publicHolidayDao.existsByDate(dto.getHolidayDate())) {
            throw new DuplicateResourceException("Holiday already exists for date: " + dto.getHolidayDate());
        }
        return mapper.toDto(publicHolidayDao.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicHolidayDto> getHolidayById(Long id) {
        return publicHolidayDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHolidayDto> getHolidaysByYear(int year) {
        return publicHolidayDao.findByYear(year).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHolidayDto> getActiveHolidaysByYear(int year) {
        return publicHolidayDao.findActiveByYear(year).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public PublicHolidayDto updateHoliday(Long id, PublicHolidayDto dto) {
        PublicHoliday holiday = publicHolidayDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
        mapper.updateEntity(dto, holiday);
        return mapper.toDto(publicHolidayDao.save(holiday));
    }

    @Override
    public void deleteHoliday(Long id) {
        publicHolidayDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
        publicHolidayDao.deleteById(id);
    }
}
