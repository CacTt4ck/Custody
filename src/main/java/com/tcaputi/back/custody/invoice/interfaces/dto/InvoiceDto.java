package com.tcaputi.back.custody.invoice.interfaces.dto;

import com.tcaputi.back.custody.invoice.domain.model.Invoice;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceStatus;
import com.tcaputi.back.custody.invoice.domain.model.InvoiceType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Invoice}
 */
public record InvoiceDto(UUID id,
                         String number,
                         InvoiceType type,
                         UUID clientId,
                         UUID projectId,
                         LocalDate issueDate,
                         LocalDate supplyDate,
                         LocalDate dueDate,
                         String currency,
                         String exchangeRateProvider,
                         BigDecimal exchangeRateValue,
                         List<InvoiceLineDto> lines,
                         BigDecimal subtotal,
                         BigDecimal taxTotal,
                         BigDecimal total,
                         String paymentTerms,
                         BigDecimal lateFeeRate,
                         BigDecimal collectionFeeEUR,
                         InvoiceStatus status,
                         String notes,
                         List<String> legalMentions) implements Serializable {
}