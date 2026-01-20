package com.tcaputi.back.custody.invoice.domain.model;

import lombok.Getter;

@Getter
public enum InvoiceType {
    FACTURE("FA"),      // Facture normale
    ACOMPTE("AV"),      // Acompte
    AVOIR("AV"),        // Avoir (note de crédit)
    FACTURE_FINALE("FA"); // Facture finale après acomptes

    private final String prefix;

    InvoiceType(String prefix) {
        this.prefix = prefix;
    }
}
