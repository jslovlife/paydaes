package com.paydaes.corehr.commondb.service.impl;

import com.paydaes.corehr.commondb.mapper.LeaveTypeMapper;
import com.paydaes.corehr.commondb.service.LeaveTypeService;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.corehr.tenant.annotation.UseCommonDb;
import com.paydaes.entities.dao.commondb.LeaveTypeDao;
import com.paydaes.entities.dto.commondb.LeaveTypeDto;
import com.paydaes.entities.model.commondb.LeaveType;
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
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeDao leaveTypeDao;
    private final LeaveTypeMapper mapper;

    @Override
    public LeaveTypeDto createLeaveType(LeaveTypeDto dto) {
        if (leaveTypeDao.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("Leave type code already exists: " + dto.getCode());
        }
        return mapper.toDto(leaveTypeDao.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeaveTypeDto> getLeaveTypeById(Long id) {
        return leaveTypeDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeaveTypeDto> getLeaveTypeByCode(String code) {
        return leaveTypeDao.findByCode(code).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeDto> getAllLeaveTypes() {
        return leaveTypeDao.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeDto> getAllActiveLeaveTypes() {
        return leaveTypeDao.findAllActive().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LeaveTypeDto updateLeaveType(Long id, LeaveTypeDto dto) {
        LeaveType leaveType = leaveTypeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + id));
        mapper.updateEntity(dto, leaveType);
        return mapper.toDto(leaveTypeDao.save(leaveType));
    }

    @Override
    public LeaveTypeDto toggleLeaveType(Long id) {
        LeaveType leaveType = leaveTypeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + id));
        leaveType.setActive(!leaveType.isActive());
        return mapper.toDto(leaveTypeDao.save(leaveType));
    }

    @Override
    public void deleteLeaveType(Long id) {
        leaveTypeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + id));
        leaveTypeDao.deleteById(id);
    }
}
