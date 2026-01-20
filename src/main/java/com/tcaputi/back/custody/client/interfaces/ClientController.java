package com.tcaputi.back.custody.client.interfaces;

import com.tcaputi.back.custody.client.application.ClientService;
import com.tcaputi.back.custody.client.interfaces.dto.ClientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<Page<ClientDto>> getClients(
            @RequestParam(defaultValue = "0") Integer page, 
            @RequestParam(defaultValue = "20") Integer size) {
        Page<ClientDto> clients = clientService.getClients(page, size);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/count")
    public int getClientCount() {
        return clientService.getClientCount();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable UUID id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClients(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String legalName,
            @RequestParam(required = false) String contactName) {
        
        try {
            List<ClientDto> clients = clientService.searchClients(email, legalName, contactName);
            return ResponseEntity.ok(clients);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto dto) {
        ClientDto createdClient = clientService.createClient(dto);
        return ResponseEntity.created(URI.create("/clients/" + createdClient.id()))
                .body(createdClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        boolean deleted = clientService.deleteClient(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable UUID id, @RequestBody ClientDto dto) {
        try {
            return clientService.updateClient(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
