package com.tcaputi.back.custody.client.application;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.client.infrastructure.ClientMapper;
import com.tcaputi.back.custody.client.infrastructure.ClientRepository;
import com.tcaputi.back.custody.client.interfaces.dto.ClientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public Page<ClientDto> getClients(Integer page, Integer size) {
        Page<Client> clients = clientRepository.findAll(PageRequest.of(page, size));
        return clients.map(clientMapper::toDto);
    }

    public Optional<ClientDto> getClientById(UUID id) {
        return clientRepository.findById(id)
                .map(clientMapper::toDto);
    }

    public Integer getClientCount() {
        return (int) clientRepository.count();
    }

    public List<ClientDto> searchClients(String email, String legalName, String contactName) {
        if (isAllParametersEmpty(email, legalName, contactName)) {
            throw new IllegalArgumentException("Au moins un critère de recherche doit être fourni");
        }

        String cleanEmail = cleanParameter(email);
        String cleanLegalName = cleanParameter(legalName);
        String cleanContactName = cleanParameter(contactName);

        log.debug("Recherche de clients avec les critères : email={}, legalName={}, contactName={}", 
                cleanEmail, cleanLegalName, cleanContactName);

        List<Client> clients = clientRepository.findClientsByMultipleCriteria(
                cleanEmail, cleanLegalName, cleanContactName
        );

        return clients.stream()
                .map(clientMapper::toDto)
                .toList();
    }

    public ClientDto createClient(ClientDto dto) {
        log.debug("Création d'un nouveau client : {}", dto.legalName());
        
        Client entity = clientMapper.toEntity(dto);
        Client savedClient = clientRepository.save(entity);
        
        log.info("Client créé avec l'ID : {}", savedClient.getId());
        return clientMapper.toDto(savedClient);
    }

    public boolean deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            log.warn("Tentative de suppression d'un client inexistant : {}", id);
            return false;
        }
        
        log.debug("Suppression du client : {}", id);
        clientRepository.deleteById(id);
        return true;
    }

    public Optional<ClientDto> updateClient(UUID id, ClientDto dto) {
        if (!id.equals(dto.id())) {
            throw new IllegalArgumentException("L'ID dans l'URL ne correspond pas à l'ID dans le body");
        }

        if (!clientRepository.existsById(id)) {
            log.warn("Tentative de mise à jour d'un client inexistant : {}", id);
            return Optional.empty();
        }

        log.debug("Mise à jour du client : {}", id);
        Client entity = clientMapper.toEntity(dto);
        Client updatedClient = clientRepository.save(entity);
        
        return Optional.of(clientMapper.toDto(updatedClient));
    }

    private boolean isAllParametersEmpty(String... parameters) {
        for (String param : parameters) {
            if (param != null && !param.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String cleanParameter(String parameter) {
        return (parameter != null && !parameter.trim().isEmpty()) ? parameter.trim() : null;
    }
}
