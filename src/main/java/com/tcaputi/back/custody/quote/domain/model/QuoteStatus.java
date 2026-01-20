package com.tcaputi.back.custody.quote.domain.model;

/**
 * Statut d’un devis.
 * DRAFT -> SENT -> (ACCEPTED | REJECTED | EXPIRED)
 */
public enum QuoteStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED
}