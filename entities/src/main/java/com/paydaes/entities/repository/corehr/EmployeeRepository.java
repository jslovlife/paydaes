package com.paydaes.entities.repository.corehr;

import com.paydaes.entities.model.corehr.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartment(String department);

    List<Employee> findByStatus(Employee.EmployeeStatus status);

    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Employee> findByNameContaining(String name);

    List<Employee> findByDepartmentAndStatus(String department, Employee.EmployeeStatus status);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    long countByStatus(Employee.EmployeeStatus status);
}
