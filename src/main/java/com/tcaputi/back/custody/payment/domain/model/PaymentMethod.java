package com.tcaputi.back.custody.payment.domain.model;

/**
 * Méthode de paiement enregistrée.
 * TRANSFER couvre virement bancaire classique ; SEPA pour virement/prélèvement SEPA si tu veux distinguer.
 */
public enum PaymentMethod {
    SEPA,
    CARD,
    TRANSFER,
    CASH
}