package com.tcaputi.back.custody.client.domain.model;

import com.tcaputi.back.custody.client.domain.model.embedded.Address;
import com.tcaputi.back.custody.client.interfaces.dto.CompanyDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity for {@link CompanyDto}
 * @author tcaputi
 *
 * <p>Ta fiche d’identité légale en tant qu’émetteur de factures/devis.</p>
 */
@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_profiles")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String legalName; // Nom légal

    private String tradeName; // Nom commercial

    @Column(length = 9, nullable = false)
    private String siren;

    @Column(length = 14, nullable = false)
    private String siret;

    private String vatNumber; // FRxx...

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "street")),
            @AttributeOverride(name = "zip", column = @Column(name = "zip")),
            @AttributeOverride(name = "city", column = @Column(name = "city")),
            @AttributeOverride(name = "country", column = @Column(name = "country"))
    })
    private Address address;

    private String email;
    private String phone;
    private String website;

    private String iban;
    private String bic;

    private String rcsOrRm; // RCS Marseille ou RM
    private String apeNaf; // Code APE/Naf
    private String legalForm; // EURL, SASU...

}
