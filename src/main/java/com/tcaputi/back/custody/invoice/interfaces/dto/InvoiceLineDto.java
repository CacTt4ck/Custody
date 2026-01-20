package com.tcaputi.back.custody.invoice.interfaces.dto;

import com.tcaputi.back.custody.invoice.domain.model.InvoiceLine;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link InvoiceLine}
 */
public record InvoiceLineDto(UUID id,
                             UUID invoiceId,
                             String designation,
                             BigDecimal quantity,
                             String unit,
                             BigDecimal unitPrice,
                             BigDecimal taxRate,
                             BigDecimal discount) implements Serializable {
}