package com.tcaputi.back.custody.project.domain.model;

/**
 * Statut d’un projet.
 * PLANNED -> ACTIVE -> (ON_HOLD <-> ACTIVE) -> DONE
 */
public enum ProjectStatus {
    PLANNED,
    ACTIVE,
    ON_HOLD,
    DONE
}