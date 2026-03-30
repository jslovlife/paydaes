package com.paydaes.entities.dao.corehr;

import com.paydaes.entities.model.corehr.Employee;
import com.paydaes.entities.repository.corehr.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmployeeDao {

    private final EmployeeRepository employeeRepository;

    public Employee save(Employee employee) { return employeeRepository.save(employee); }

    public Optional<Employee> findById(Long id) { return employeeRepository.findById(id); }

    public Optional<Employee> findByEmployeeId(String employeeId) { return employeeRepository.findByEmployeeId(employeeId); }

    public Optional<Employee> findByEmail(String email) { return employeeRepository.findByEmail(email); }

    public List<Employee> findAll() { return employeeRepository.findAll(); }

    public List<Employee> findByDepartment(String department) { return employeeRepository.findByDepartment(department); }

    public List<Employee> findByStatus(Employee.EmployeeStatus status) { return employeeRepository.findByStatus(status); }

    public List<Employee> findByNameContaining(String name) { return employeeRepository.findByNameContaining(name); }

    public List<Employee> findByDepartmentAndStatus(String department, Employee.EmployeeStatus status) { return employeeRepository.findByDepartmentAndStatus(department, status); }

    public boolean existsByEmployeeId(String employeeId) { return employeeRepository.existsByEmployeeId(employeeId); }

    public boolean existsByEmail(String email) { return employeeRepository.existsByEmail(email); }

    public void deleteById(Long id) { employeeRepository.deleteById(id); }

    public long countByStatus(Employee.EmployeeStatus status) { return employeeRepository.countByStatus(status); }
}
