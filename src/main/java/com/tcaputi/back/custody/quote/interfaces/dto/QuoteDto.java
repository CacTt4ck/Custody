package com.tcaputi.back.custody.quote.interfaces.dto;

import com.tcaputi.back.custody.quote.domain.model.Quote;
import com.tcaputi.back.custody.quote.domain.model.QuoteStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Quote}
 */
public record QuoteDto(UUID id,
                       String number,
                       UUID clientId,
                       UUID projectId,
                       LocalDate issueDate,
                       LocalDate validUntil,
                       String currency,
                       String exchangeRateProvider,
                       BigDecimal exchangeRateValue,
                       List<QuoteLineDto> lines,
                       BigDecimal subtotal,
                       BigDecimal taxTotal,
                       BigDecimal total,
                       QuoteStatus status,
                       String terms) implements Serializable {
}