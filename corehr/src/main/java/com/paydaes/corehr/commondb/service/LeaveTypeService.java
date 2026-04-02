package com.paydaes.corehr.commondb.service;

import com.paydaes.entities.dto.commondb.LeaveTypeDto;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeService {

    LeaveTypeDto createLeaveType(LeaveTypeDto dto);

    Optional<LeaveTypeDto> getLeaveTypeById(Long id);

    Optional<LeaveTypeDto> getLeaveTypeByCode(String code);

    List<LeaveTypeDto> getAllLeaveTypes();

    List<LeaveTypeDto> getAllActiveLeaveTypes();

    LeaveTypeDto updateLeaveType(Long id, LeaveTypeDto dto);

    LeaveTypeDto toggleLeaveType(Long id);

    void deleteLeaveType(Long id);
}
