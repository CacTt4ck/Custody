package com.tcaputi.back.custody.tax.domain.model;

/**
 * Type de taxation appliqué à la facture/devis.
 * - FR_TVA_STANDARD : TVA française (20% par défaut, configurable)
 * - FR_TVA_FRANCHISE_293B : TVA non applicable – art. 293 B CGI (montants HT = TTC)
 * - EU_REVERSE_CHARGE : autoliquidation intra-UE (HT, mention reverse charge)
 * - NON_EU_EXPORT_259_1 : export de services hors UE (0%, mention art. 259-1 CGI)
 */
public enum TaxType {
    FR_TVA_STANDARD,
    FR_TVA_FRANCHISE_293B,
    EU_REVERSE_CHARGE,
    NON_EU_EXPORT_259_1
}
