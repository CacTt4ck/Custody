package com.tcaputi.back.custody.client.interfaces.dto;

import com.tcaputi.back.custody.client.domain.model.embedded.Address;

import java.io.Serializable;

/**
 * DTO for {@link Address}
 */
public record AddressDto(String street, String zip, String city, String country) implements Serializable {
}