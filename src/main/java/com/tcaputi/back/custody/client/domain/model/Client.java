package com.tcaputi.back.custody.client.domain.model;

import com.tcaputi.back.custody.client.domain.model.embedded.Address;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String legalName;

    private String vatNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
            @AttributeOverride(name = "zip", column = @Column(name = "billing_zip")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
            @AttributeOverride(name = "zip", column = @Column(name = "shipping_zip")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;

    private String defaultCurrency; // EUR, USD...
    private String locale; // fr-FR, en-US

    private String contactName;
    private String contactEmail;
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String notes;

}
