package com.tcaputi.back.custody.project.infrastructure;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.project.interfaces.dto.ProjectDto;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    
    @Mapping(source = "client.id", target = "clientId")
    ProjectDto toDto(Project project);

    @Mapping(target = "client", ignore = true) // On l'assignera manuellement dans le service
    Project toEntity(ProjectDto projectDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "client", ignore = true) // On l'assignera manuellement dans le service
    Project partialUpdate(ProjectDto projectDto, @MappingTarget Project project);
    
    // Méthodes utilitaires pour le mapping du client
    default UUID mapClientToClientId(Client client) {
        return client != null ? client.getId() : null;
    }
    
    default Client mapClientIdToClient(UUID clientId) {
        if (clientId == null) return null;
        Client client = new Client();
        client.setId(clientId);
        return client;
    }
}