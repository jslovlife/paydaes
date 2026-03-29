package com.paydaes.entities.repository;

import com.paydaes.entities.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByEmail(String email);
    
    List<Client> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Client c WHERE c.phoneNumber = :phoneNumber")
    Optional<Client> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(c) FROM Client c")
    long countTotalClients();
}