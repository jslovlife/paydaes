package com.paydaes.corehr.companydb.service.impl;

import com.paydaes.corehr.companydb.mapper.EmployeeMapper;
import com.paydaes.corehr.companydb.service.EmployeeService;
import com.paydaes.corehr.exception.DuplicateResourceException;
import com.paydaes.corehr.exception.ResourceNotFoundException;
import com.paydaes.entities.dao.corehr.EmployeeDao;
import com.paydaes.entities.dto.corehr.EmployeeDto;
import com.paydaes.entities.model.corehr.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDao employeeDao;
    private final EmployeeMapper mapper;

    @Override
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeDao.existsByEmployeeId(dto.getEmployeeId())) {
            throw new DuplicateResourceException(
                "Employee with ID already exists: " + dto.getEmployeeId());
        }
        if (employeeDao.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(
                "Employee with email already exists: " + dto.getEmail());
        }

        return mapper.toDto(employeeDao.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        return employeeDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeByEmployeeId(String employeeId) {
        return employeeDao.findByEmployeeId(employeeId).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeByEmail(String email) {
        return employeeDao.findByEmail(email).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeDao.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByDepartment(String department) {
        return employeeDao.findByDepartment(department).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByStatus(Employee.EmployeeStatus status) {
        return employeeDao.findByStatus(status).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> searchEmployeesByName(String name) {
        return employeeDao.findByNameContaining(name).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        mapper.updateEntity(dto, employee);

        return mapper.toDto(employeeDao.save(employee));
    }

    @Override
    public EmployeeDto updateEmployeeStatus(Long id, Employee.EmployeeStatus status) {
        Employee employee = employeeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employee.setStatus(status);
        return mapper.toDto(employeeDao.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employeeDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEmployeeCountByStatus(Employee.EmployeeStatus status) {
        return employeeDao.countByStatus(status);
    }
}
