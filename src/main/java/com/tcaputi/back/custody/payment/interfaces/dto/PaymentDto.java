package com.tcaputi.back.custody.payment.interfaces.dto;

import com.tcaputi.back.custody.payment.domain.model.Payment;
import com.tcaputi.back.custody.payment.domain.model.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link Payment}
 */
public record PaymentDto(UUID id,
                         UUID invoiceId,
                         LocalDate date,
                         PaymentMethod method,
                         String currency,
                         BigDecimal amount,
                         BigDecimal fxRateAtPayment) implements Serializable {
}