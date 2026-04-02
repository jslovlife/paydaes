package com.paydaes.entities.repository.tms;

import com.paydaes.entities.model.tms.CompanyDbConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyDbConnectionRepository extends JpaRepository<CompanyDbConnection, Long> {

    Optional<CompanyDbConnection> findByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);
}
