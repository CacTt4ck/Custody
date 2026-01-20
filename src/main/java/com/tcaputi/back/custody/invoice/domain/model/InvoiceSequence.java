package com.tcaputi.back.custody.invoice.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice_sequences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"type", "year"}))
public class InvoiceSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Long currentNumber;

    @Version
    private Long version; // Pour la gestion de la concurrence
}
