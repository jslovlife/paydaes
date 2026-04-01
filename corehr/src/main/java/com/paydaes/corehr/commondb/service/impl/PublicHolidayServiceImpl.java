package com.paydaes.corehr.commondb.service.impl;

import com.paydaes.corehr.commondb.mapper.PublicHolidayMapper;
import com.paydaes.corehr.commondb.service.PublicHolidayService;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.corehr.tenant.TenantContext;
import com.paydaes.entities.dao.commondb.PublicHolidayDao;
import com.paydaes.entities.dto.commondb.PublicHolidayDto;
import com.paydaes.entities.model.commondb.PublicHoliday;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PublicHolidayServiceImpl implements PublicHolidayService {

    private final PublicHolidayDao    publicHolidayDao;
    private final PublicHolidayMapper mapper;

    @Override
    public PublicHolidayDto addHoliday(PublicHolidayDto dto) {
        TenantContext.useCommonDb();
        try {
            if (publicHolidayDao.existsByDate(dto.getHolidayDate())) {
                throw new DuplicateResourceException("Holiday already exists for date: " + dto.getHolidayDate());
            }
            return mapper.toDto(publicHolidayDao.save(mapper.toEntity(dto)));
        } finally {
            TenantContext.useCompanyDb();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicHolidayDto> getHolidayById(Long id) {
        TenantContext.useCommonDb();
        try {
            return publicHolidayDao.findById(id).map(mapper::toDto);
        } finally {
            TenantContext.useCompanyDb();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHolidayDto> getHolidaysByYear(int year) {
        TenantContext.useCommonDb();
        try {
            return publicHolidayDao.findByYear(year).stream()
                    .map(mapper::toDto).collect(Collectors.toList());
        } finally {
            TenantContext.useCompanyDb();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHolidayDto> getActiveHolidaysByYear(int year) {
        TenantContext.useCommonDb();
        try {
            return publicHolidayDao.findActiveByYear(year).stream()
                    .map(mapper::toDto).collect(Collectors.toList());
        } finally {
            TenantContext.useCompanyDb();
        }
    }

    @Override
    public PublicHolidayDto updateHoliday(Long id, PublicHolidayDto dto) {
        TenantContext.useCommonDb();
        try {
            PublicHoliday holiday = publicHolidayDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
            mapper.updateEntity(dto, holiday);
            return mapper.toDto(publicHolidayDao.save(holiday));
        } finally {
            TenantContext.useCompanyDb();
        }
    }

    @Override
    public void deleteHoliday(Long id) {
        TenantContext.useCommonDb();
        try {
            publicHolidayDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
            publicHolidayDao.deleteById(id);
        } finally {
            TenantContext.useCompanyDb();
        }
    }
}
