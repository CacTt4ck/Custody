package com.tcaputi.back.custody.payment.domain.model;

import com.tcaputi.back.custody.invoice.domain.model.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Invoice invoice;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String currency;
    private BigDecimal amount;
    private BigDecimal fxRateAtPayment;
}
