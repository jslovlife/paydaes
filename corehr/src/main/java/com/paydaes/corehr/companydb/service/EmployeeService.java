package com.paydaes.corehr.companydb.service;

import com.paydaes.entities.dto.corehr.EmployeeDto;
import com.paydaes.entities.model.corehr.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    EmployeeDto createEmployee(EmployeeDto employeeDto);

    Optional<EmployeeDto> getEmployeeById(Long id);

    Optional<EmployeeDto> getEmployeeByEmployeeId(String employeeId);

    Optional<EmployeeDto> getEmployeeByEmail(String email);

    List<EmployeeDto> getAllEmployees();

    List<EmployeeDto> getEmployeesByDepartment(String department);

    List<EmployeeDto> getEmployeesByStatus(Employee.EmployeeStatus status);

    List<EmployeeDto> searchEmployeesByName(String name);

    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    EmployeeDto updateEmployeeStatus(Long id, Employee.EmployeeStatus status);

    void deleteEmployee(Long id);

    long getEmployeeCountByStatus(Employee.EmployeeStatus status);
}
