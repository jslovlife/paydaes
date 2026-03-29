package com.paydaes.tms.service;

import com.paydaes.entities.dao.ClientDao;
import com.paydaes.entities.dto.ClientDto;
import com.paydaes.entities.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientService {
    
    @Autowired
    private ClientDao clientDao;
    
    public ClientDto createClient(ClientDto clientDto) {
        // Validate if email already exists
        if (clientDao.existsByEmail(clientDto.getEmail())) {
            throw new RuntimeException("Client with email already exists: " + clientDto.getEmail());
        }
        
        Client client = new Client(
            clientDto.getName(),
            clientDto.getEmail(),
            clientDto.getPhoneNumber()
        );
        
        Client savedClient = clientDao.save(client);
        return convertToDto(savedClient);
    }
    
    public Optional<ClientDto> getClientById(Long id) {
        return clientDao.findById(id).map(this::convertToDto);
    }
    
    public Optional<ClientDto> getClientByEmail(String email) {
        return clientDao.findByEmail(email).map(this::convertToDto);
    }
    
    public List<ClientDto> getAllClients() {
        return clientDao.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ClientDto> searchClientsByName(String name) {
        return clientDao.findByNameContaining(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        Optional<Client> existingClient = clientDao.findById(id);
        if (existingClient.isEmpty()) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        
        Client client = existingClient.get();
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setPhoneNumber(clientDto.getPhoneNumber());
        client.setUpdatedAt(LocalDateTime.now());
        
        Client updatedClient = clientDao.save(client);
        return convertToDto(updatedClient);
    }
    
    public void deleteClient(Long id) {
        if (!clientDao.findById(id).isPresent()) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        clientDao.deleteById(id);
    }
    
    public long getTotalClientCount() {
        return clientDao.countTotalClients();
    }
    
    private ClientDto convertToDto(Client client) {
        return new ClientDto(
            client.getId(),
            client.getName(),
            client.getEmail(),
            client.getPhoneNumber(),
            client.getCreatedAt(),
            client.getUpdatedAt()
        );
    }
}