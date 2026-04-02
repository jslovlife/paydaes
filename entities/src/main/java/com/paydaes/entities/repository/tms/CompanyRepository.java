package com.paydaes.entities.repository.tms;

import com.paydaes.entities.model.tms.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByClientId(Long clientId);

    boolean existsByNameAndClientId(String name, Long clientId);
}
