package com.tcaputi.back.custody.project.domain.model;

/**
 * Mode de facturation d’un projet.
 * TIME_AND_MATERIALS : TJM/TH + temps réel facturé.
 * FIXED_PRICE : prix forfaitaire, milestones éventuels.
 */
public enum BillingMode {
    TIME_AND_MATERIALS,
    FIXED_PRICE
}