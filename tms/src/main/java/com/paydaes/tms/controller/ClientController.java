package com.paydaes.tms.controller;

import com.paydaes.entities.dto.tms.ClientDto;
import com.paydaes.tms.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tms/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        return new ResponseEntity<>(clientService.createClient(clientDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClients(@RequestParam String name) {
        return ResponseEntity.ok(clientService.searchClientsByName(name));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientDto> getClientByEmail(@PathVariable String email) {
        return clientService.getClientByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        return ResponseEntity.ok(clientService.updateClient(id, clientDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalClientCount() {
        return ResponseEntity.ok(clientService.getTotalClientCount());
    }
}
