package com.paydaes.corehr.controller;

import com.paydaes.entities.dto.EmployeeDto;
import com.paydaes.entities.model.Employee;
import com.paydaes.corehr.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/corehr/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto employeeDto) {
        try {
            EmployeeDto createdEmployee = employeeService.createEmployee(employeeDto);
            return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        Optional<EmployeeDto> employee = employeeService.getEmployeeById(id);
        return employee.map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                      .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<EmployeeDto> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        Optional<EmployeeDto> employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return employee.map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                      .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(@PathVariable String department) {
        List<EmployeeDto> employees = employeeService.getEmployeesByDepartment(department);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByStatus(@PathVariable Employee.EmployeeStatus status) {
        List<EmployeeDto> employees = employeeService.getEmployeesByStatus(status);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployees(@RequestParam String name) {
        List<EmployeeDto> employees = employeeService.searchEmployeesByName(name);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeDto> getEmployeeByEmail(@PathVariable String email) {
        Optional<EmployeeDto> employee = employeeService.getEmployeeByEmail(email);
        return employee.map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                      .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto employeeDto) {
        try {
            EmployeeDto updatedEmployee = employeeService.updateEmployee(id, employeeDto);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeDto> updateEmployeeStatus(@PathVariable Long id, @RequestParam Employee.EmployeeStatus status) {
        try {
            EmployeeDto updatedEmployee = employeeService.updateEmployeeStatus(id, status);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getEmployeeCountByStatus(@PathVariable Employee.EmployeeStatus status) {
        long count = employeeService.getEmployeeCountByStatus(status);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}