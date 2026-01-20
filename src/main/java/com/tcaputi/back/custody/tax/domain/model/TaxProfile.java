package com.tcaputi.back.custody.tax.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tax_profiles")
public class TaxProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TaxType type;

    private String label;

    @ElementCollection
    private List<String> mandatoryMentions = new ArrayList<>();
}
