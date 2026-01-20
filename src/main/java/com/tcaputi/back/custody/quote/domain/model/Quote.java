package com.tcaputi.back.custody.quote.domain.model;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.project.domain.model.Project;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente un devis avec lignes détaillées.
 */
@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String number; // Q-2025-001

    @ManyToOne(optional = false)
    private Client client;

    @ManyToOne
    private Project project;

    private LocalDate issueDate;
    private LocalDate validUntil;

    private String currency;
    private String exchangeRateProvider;
    private BigDecimal exchangeRateValue;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteLine> lines = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    @Column(columnDefinition = "TEXT")
    private String terms;

}
