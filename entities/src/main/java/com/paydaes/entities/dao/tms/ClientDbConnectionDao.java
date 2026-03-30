package com.paydaes.entities.dao.tms;

import com.paydaes.entities.model.tms.ClientDbConnection;
import com.paydaes.entities.repository.tms.ClientDbConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientDbConnectionDao {

    private final ClientDbConnectionRepository repository;

    public ClientDbConnection save(ClientDbConnection connection) { return repository.save(connection); }

    public Optional<ClientDbConnection> findByClientId(Long clientId) { return repository.findByClientId(clientId); }

    public Optional<ClientDbConnection> findById(Long id) { return repository.findById(id); }

    public List<ClientDbConnection> findAll() { return repository.findAll(); }

    public boolean existsByClientId(Long clientId) { return repository.existsByClientId(clientId); }

    public void deleteById(Long id) { repository.deleteById(id); }
}
