package com.tcaputi.back.custody.project.interfaces;

import com.tcaputi.back.custody.project.application.ProjectService;
import com.tcaputi.back.custody.project.domain.model.ProjectStatus;
import com.tcaputi.back.custody.project.interfaces.dto.ProjectDto;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getProjects(
            @RequestParam(defaultValue = "0") Integer page, 
            @RequestParam(defaultValue = "20") Integer size) {
        Page<ProjectDto> projects = projectService.getProjects(page, size);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/count/{clientId}")
    public int getProjectCount(@PathVariable UUID clientId) {
        return projectService.getProjectCountByClientId(clientId);
    }

    @GetMapping("/count/status/{status}")
    public int getProjectCountByStatus(@PathVariable ProjectStatus status) {
        return projectService.getProjectCountByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProjectDto>> getProjectsByClient(@PathVariable UUID clientId) {
        List<ProjectDto> projects = projectService.getProjectsByClient(clientId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectDto>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        List<ProjectDto> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProjectDto>> getActiveProjects() {
        List<ProjectDto> projects = projectService.getActiveProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectDto>> searchProjects(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name) {
        
        List<ProjectDto> projects = projectService.searchProjects(clientId, status, code, name);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectDto dto) {
        try {
            ProjectDto createdProject = projectService.createProject(dto);
            return ResponseEntity.created(URI.create("/projects/" + createdProject.id()))
                    .body(createdProject);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la création du projet : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        boolean deleted = projectService.deleteProject(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable UUID id, @RequestBody ProjectDto dto) {
        try {
            return projectService.updateProject(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la mise à jour du projet : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
