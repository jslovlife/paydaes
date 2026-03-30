package com.paydaes.tms.service.impl;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dto.tms.ClientDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;

    @Override
    public ClientDto createClient(ClientDto clientDto) {
        if (clientDao.existsByEmail(clientDto.getEmail())) {
            throw new DuplicateResourceException(
                "Client with email already exists: " + clientDto.getEmail());
        }
        Client client = new Client(clientDto.getName(), clientDto.getEmail(), clientDto.getPhoneNumber());
        return toDto(clientDao.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientById(Long id) {
        return clientDao.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientByEmail(String email) {
        return clientDao.findByEmail(email).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients() {
        return clientDao.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> searchClientsByName(String name) {
        return clientDao.findByNameContaining(name).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        Client client = clientDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setPhoneNumber(clientDto.getPhoneNumber());
        return toDto(clientDao.save(client));
    }

    @Override
    public void deleteClient(Long id) {
        clientDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        clientDao.deleteById(id);
    }

    @Override
    public long getTotalClientCount() {
        return clientDao.countTotalClients();
    }

    private ClientDto toDto(Client client) {
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
