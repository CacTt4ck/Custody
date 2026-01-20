package com.tcaputi.back.custody.quote.interfaces.dto;

import com.tcaputi.back.custody.quote.domain.model.QuoteLine;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link QuoteLine}
 */
public record QuoteLineDto(UUID id,
                           String designation,
                           BigDecimal quantity,
                           String unit,
                           BigDecimal unitPrice,
                           BigDecimal taxRate,
                           BigDecimal discount) implements Serializable {
}