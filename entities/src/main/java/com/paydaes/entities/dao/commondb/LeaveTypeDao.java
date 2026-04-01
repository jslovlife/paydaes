package com.paydaes.entities.dao.commondb;

import com.paydaes.entities.model.commondb.LeaveType;
import com.paydaes.entities.repository.commondb.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeaveTypeDao {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveType save(LeaveType leaveType)                     { return leaveTypeRepository.save(leaveType); }

    public Optional<LeaveType> findById(Long id)                   { return leaveTypeRepository.findById(id); }

    public Optional<LeaveType> findByCode(String code)             { return leaveTypeRepository.findByCode(code); }

    public List<LeaveType> findAll()                               { return leaveTypeRepository.findAll(); }

    public List<LeaveType> findAllActive()                         { return leaveTypeRepository.findByIsActive(true); }

    public boolean existsByCode(String code)                       { return leaveTypeRepository.existsByCode(code); }

    public void deleteById(Long id)                                { leaveTypeRepository.deleteById(id); }
}
