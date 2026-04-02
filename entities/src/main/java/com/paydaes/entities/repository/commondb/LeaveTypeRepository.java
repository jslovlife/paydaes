package com.paydaes.entities.repository.commondb;

import com.paydaes.entities.model.commondb.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByCode(String code);

    List<LeaveType> findByIsActive(boolean isActive);

    boolean existsByCode(String code);
}
