package com.tcaputi.back.custody.invoice.domain.model;

/**
 * Statut d’une facture.
 * DRAFT -> SENT -> (PARTIALLY_PAID -> PAID) ou OVERDUE ; CANCELLED via avoir
 * Remarque: légalement, on évite de "supprimer" une facture émise ; on passe par un avoir.
 */
public enum InvoiceStatus {
    DRAFT,
    SENT,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED
}