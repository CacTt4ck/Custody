package com.tcaputi.back.custody.invoice.domain.model;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.project.domain.model.Project;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type; // Nouveau champ

    @ManyToOne(optional = false)
    private Client client;

    @ManyToOne
    private Project project;

    private LocalDate issueDate;
    private LocalDate supplyDate;
    private LocalDate dueDate;

    private String currency;
    private String exchangeRateProvider;
    private BigDecimal exchangeRateValue;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal total;

    private String paymentTerms;
    private BigDecimal lateFeeRate;
    private BigDecimal collectionFeeEUR;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ElementCollection
    private List<String> legalMentions = new ArrayList<>();
}

