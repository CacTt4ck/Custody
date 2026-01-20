package com.tcaputi.back.custody.project.infrastructure;

import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.project.domain.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Recherche par client
    Page<Project> findByClientId(UUID clientId, Pageable pageable);
    List<Project> findByClientId(UUID clientId);

    // Recherche par statut
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    List<Project> findByStatus(ProjectStatus status);

    // Recherche par code (unique)
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query("SELECT p FROM Project p WHERE " +
            "(:clientId IS NULL OR p.client.id = :clientId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:code IS NULL OR UPPER(p.code) LIKE UPPER(CONCAT('%', :code, '%'))) AND " +
            "(:name IS NULL OR UPPER(p.name) LIKE UPPER(CONCAT('%', :name, '%')))")
    List<Project> findProjectsByMultipleCriteria(
            @Param("clientId") UUID clientId,
            @Param("status") ProjectStatus status,
            @Param("code") String code,
            @Param("name") String name);

    // Recherche par plage de dates
    List<Project> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Project> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

    // Projets actifs (en cours)
    @Query("SELECT p FROM Project p WHERE p.status IN ('ACTIVE', 'IN_PROGRESS')")
    List<Project> findActiveProjects();

    int countProjectsByClientId(UUID clientId);

    int countProjectsByStatus(ProjectStatus status);

}