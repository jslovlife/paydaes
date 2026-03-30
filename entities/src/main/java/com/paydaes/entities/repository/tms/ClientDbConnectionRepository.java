package com.paydaes.entities.repository.tms;

import com.paydaes.entities.model.tms.ClientDbConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientDbConnectionRepository extends JpaRepository<ClientDbConnection, Long> {

    Optional<ClientDbConnection> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}
