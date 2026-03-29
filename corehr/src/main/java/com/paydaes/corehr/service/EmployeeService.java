package com.paydaes.corehr.service;

import com.paydaes.entities.dao.EmployeeDao;
import com.paydaes.entities.dto.EmployeeDto;
import com.paydaes.entities.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmployeeService {
    
    private final EmployeeDao employeeDao;
    
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        // Validate if employee ID already exists
        if (employeeDao.existsByEmployeeId(employeeDto.getEmployeeId())) {
            throw new RuntimeException("Employee with ID already exists: " + employeeDto.getEmployeeId());
        }
        
        // Validate if email already exists
        if (employeeDao.existsByEmail(employeeDto.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + employeeDto.getEmail());
        }
        
        Employee employee = new Employee(
            employeeDto.getEmployeeId(),
            employeeDto.getFirstName(),
            employeeDto.getLastName(),
            employeeDto.getEmail()
        );
        
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setHireDate(employeeDto.getHireDate());
        employee.setJobTitle(employeeDto.getJobTitle());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setSalary(employeeDto.getSalary());
        
        Employee savedEmployee = employeeDao.save(employee);
        return convertToDto(savedEmployee);
    }
    
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        return employeeDao.findById(id).map(this::convertToDto);
    }
    
    public Optional<EmployeeDto> getEmployeeByEmployeeId(String employeeId) {
        return employeeDao.findByEmployeeId(employeeId).map(this::convertToDto);
    }
    
    public Optional<EmployeeDto> getEmployeeByEmail(String email) {
        return employeeDao.findByEmail(email).map(this::convertToDto);
    }
    
    public List<EmployeeDto> getAllEmployees() {
        return employeeDao.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EmployeeDto> getEmployeesByDepartment(String department) {
        return employeeDao.findByDepartment(department).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EmployeeDto> getEmployeesByStatus(Employee.EmployeeStatus status) {
        return employeeDao.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<EmployeeDto> searchEmployeesByName(String name) {
        return employeeDao.findByNameContaining(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Optional<Employee> existingEmployee = employeeDao.findById(id);
        if (existingEmployee.isEmpty()) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        
        Employee employee = existingEmployee.get();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setEmail(employeeDto.getEmail());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setHireDate(employeeDto.getHireDate());
        employee.setJobTitle(employeeDto.getJobTitle());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setSalary(employeeDto.getSalary());
        employee.setStatus(employeeDto.getStatus());
        employee.setUpdatedAt(LocalDateTime.now());
        
        Employee updatedEmployee = employeeDao.save(employee);
        return convertToDto(updatedEmployee);
    }
    
    public EmployeeDto updateEmployeeStatus(Long id, Employee.EmployeeStatus status) {
        Optional<Employee> existingEmployee = employeeDao.findById(id);
        if (existingEmployee.isEmpty()) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        
        Employee employee = existingEmployee.get();
        employee.setStatus(status);
        employee.setUpdatedAt(LocalDateTime.now());
        
        Employee updatedEmployee = employeeDao.save(employee);
        return convertToDto(updatedEmployee);
    }
    
    public void deleteEmployee(Long id) {
        if (!employeeDao.findById(id).isPresent()) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeDao.deleteById(id);
    }
    
    public long getEmployeeCountByStatus(Employee.EmployeeStatus status) {
        return employeeDao.countByStatus(status);
    }
    
    private EmployeeDto convertToDto(Employee employee) {
        return new EmployeeDto(
            employee.getId(),
            employee.getEmployeeId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getPhoneNumber(),
            employee.getHireDate(),
            employee.getJobTitle(),
            employee.getDepartment(),
            employee.getSalary(),
            employee.getStatus(),
            employee.getCreatedAt(),
            employee.getUpdatedAt()
        );
    }
}