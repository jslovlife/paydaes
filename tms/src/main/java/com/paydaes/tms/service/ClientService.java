package com.paydaes.tms.service;

import com.paydaes.entities.dto.tms.ClientDto;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    ClientDto createClient(ClientDto clientDto);

    Optional<ClientDto> getClientById(Long id);

    Optional<ClientDto> getClientByEmail(String email);

    List<ClientDto> getAllClients();

    List<ClientDto> searchClientsByName(String name);

    ClientDto updateClient(Long id, ClientDto clientDto);

    void deleteClient(Long id);

    long getTotalClientCount();
}
