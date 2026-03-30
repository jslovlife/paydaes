package com.paydaes.entities.dao.tms;

import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.repository.tms.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientDao {

    private final ClientRepository clientRepository;

    public Client save(Client client) { return clientRepository.save(client); }

    public Optional<Client> findById(Long id) { return clientRepository.findById(id); }

    public Optional<Client> findByEmail(String email) { return clientRepository.findByEmail(email); }

    public List<Client> findAll() { return clientRepository.findAll(); }

    public List<Client> findByNameContaining(String name) { return clientRepository.findByNameContainingIgnoreCase(name); }

    public boolean existsByEmail(String email) { return clientRepository.existsByEmail(email); }

    public void deleteById(Long id) { clientRepository.deleteById(id); }

    public long countTotalClients() { return clientRepository.countTotalClients(); }
}
