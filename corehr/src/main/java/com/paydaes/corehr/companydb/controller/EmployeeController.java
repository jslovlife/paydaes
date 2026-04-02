package com.paydaes.corehr.companydb.controller;

import com.paydaes.corehr.companydb.service.EmployeeService;
import com.paydaes.entities.dto.corehr.EmployeeDto;
import com.paydaes.entities.model.corehr.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corehr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto employeeDto) {
        return new ResponseEntity<>(employeeService.createEmployee(employeeDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<EmployeeDto> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        return employeeService.getEmployeeByEmployeeId(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeDto> getEmployeeByEmail(@PathVariable String email) {
        return employeeService.getEmployeeByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByStatus(@PathVariable Employee.EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployees(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchEmployeesByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id,
                                                      @RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeDto> updateEmployeeStatus(@PathVariable Long id,
                                                            @RequestParam Employee.EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.updateEmployeeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getEmployeeCountByStatus(@PathVariable Employee.EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeeCountByStatus(status));
    }
}
