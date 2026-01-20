package com.tcaputi.back.custody.project.application;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.client.infrastructure.ClientRepository;
import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.project.domain.model.ProjectStatus;
import com.tcaputi.back.custody.project.infrastructure.ProjectMapper;
import com.tcaputi.back.custody.project.infrastructure.ProjectRepository;
import com.tcaputi.back.custody.project.interfaces.dto.ProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ProjectMapper projectMapper;

    public Page<ProjectDto> getProjects(Integer page, Integer size) {
        Page<Project> projects = projectRepository.findAll(PageRequest.of(page, size));
        return projects.map(projectMapper::toDto);
    }

    public Optional<ProjectDto> getProjectById(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toDto);
    }

    public int getProjectCountByClientId(UUID clientId) {
        return projectRepository.countProjectsByClientId(clientId);
    }

    public int getProjectCountByStatus(ProjectStatus status) {
        return projectRepository.countProjectsByStatus(status);
    }

    public List<ProjectDto> getProjectsByClient(UUID clientId) {
        log.debug("Récupération des projets pour le client : {}", clientId);
        List<Project> projects = projectRepository.findByClientId(clientId);
        return projects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public List<ProjectDto> getProjectsByStatus(ProjectStatus status) {
        log.debug("Récupération des projets avec le statut : {}", status);
        List<Project> projects = projectRepository.findByStatus(status);
        return projects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public List<ProjectDto> searchProjects(UUID clientId, ProjectStatus status, String code, String name) {
        log.debug("Recherche de projets avec les critères : clientId={}, status={}, code={}, name={}", 
                clientId, status, code, name);

        // Nettoyage des paramètres
        String cleanCode = cleanParameter(code);
        String cleanName = cleanParameter(name);

        List<Project> projects = projectRepository.findProjectsByMultipleCriteria(
                clientId, status, cleanCode, cleanName
        );

        return projects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public List<ProjectDto> getActiveProjects() {
        log.debug("Récupération des projets actifs");
        List<Project> projects = projectRepository.findActiveProjects();
        return projects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    @Transactional
    public ProjectDto createProject(ProjectDto dto) {
        log.debug("Création d'un nouveau projet : {}", dto.name());

        // Validation du client
        if (!clientRepository.existsById(dto.clientId())) {
            throw new IllegalArgumentException("Le client avec l'ID " + dto.clientId() + " n'existe pas");
        }

        // Validation de l'unicité du code
        if (projectRepository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Un projet avec le code " + dto.code() + " existe déjà");
        }

        // Validation des dates
        validateProjectDates(dto);

        Project entity = projectMapper.toEntity(dto);

        // Récupération et assignation du client (obligatoire avec le premier mapping)
        Client client = clientRepository.findById(dto.clientId()).orElseThrow();
        entity.setClient(client);

        Project savedProject = projectRepository.save(entity);

        log.info("Projet créé avec l'ID : {} et le code : {}", savedProject.getId(), savedProject.getCode());
        return projectMapper.toDto(savedProject);
    }


    @Transactional
    public boolean deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            log.warn("Tentative de suppression d'un projet inexistant : {}", id);
            return false;
        }
        
        log.debug("Suppression du projet : {}", id);
        projectRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<ProjectDto> updateProject(UUID id, ProjectDto dto) {
        // Validation de cohérence ID
        if (!id.equals(dto.id())) {
            throw new IllegalArgumentException("L'ID dans l'URL ne correspond pas à l'ID dans le body");
        }

        // Vérification de l'existence du projet
        if (!projectRepository.existsById(id)) {
            log.warn("Tentative de mise à jour d'un projet inexistant : {}", id);
            return Optional.empty();
        }

        // Validation du client
        if (!clientRepository.existsById(dto.clientId())) {
            throw new IllegalArgumentException("Le client avec l'ID " + dto.clientId() + " n'existe pas");
        }

        // Validation de l'unicité du code (sauf pour le projet actuel)
        if (projectRepository.existsByCodeAndIdNot(dto.code(), id)) {
            throw new IllegalArgumentException("Un autre projet avec le code " + dto.code() + " existe déjà");
        }

        // Validation des dates
        validateProjectDates(dto);

        log.debug("Mise à jour du projet : {}", id);
        
        Project entity = projectMapper.toEntity(dto);
        
        // Récupération et assignation du client
        Client client = clientRepository.findById(dto.clientId()).orElseThrow();
        entity.setClient(client);

        Project updatedProject = projectRepository.save(entity);
        
        return Optional.of(projectMapper.toDto(updatedProject));
    }

    private void validateProjectDates(ProjectDto dto) {
        if (dto.startDate() != null && dto.endDate() != null && dto.endDate().isBefore(dto.startDate()))
                throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
    }

    private String cleanParameter(String parameter) {
        return (parameter != null && !parameter.trim().isEmpty()) ? parameter.trim() : null;
    }
}
