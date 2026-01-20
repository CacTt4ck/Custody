package com.tcaputi.back.custody.client.interfaces.dto;

import com.tcaputi.back.custody.client.domain.model.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Client}
 */
public record ClientDto(UUID id, String legalName, String vatNumber, AddressDto billingAddress,
                        AddressDto shippingAddress, String defaultCurrency, String locale, String contactName,
                        String contactEmail, String contactPhone, String notes) implements Serializable {
}