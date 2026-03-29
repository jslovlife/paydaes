package com.paydaes.tms.controller;

import com.paydaes.entities.dto.ClientDto;
import com.paydaes.tms.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tms/clients")
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        try {
            ClientDto createdClient = clientService.createClient(clientDto);
            return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        Optional<ClientDto> client = clientService.getClientById(id);
        return client.map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClients(@RequestParam String name) {
        List<ClientDto> clients = clientService.searchClientsByName(name);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ClientDto> getClientByEmail(@PathVariable String email) {
        Optional<ClientDto> client = clientService.getClientByEmail(email);
        return client.map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            ClientDto updatedClient = clientService.updateClient(id, clientDto);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalClientCount() {
        long count = clientService.getTotalClientCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}