package com.tcaputi.back.custody.client.domain.model.embedded;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String street;
    private String zip;
    private String city;
    private String country;
}
