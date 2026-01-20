package com.tcaputi.back.custody.client.interfaces.dto;

import com.tcaputi.back.custody.client.domain.model.Company;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Company}
 */
public record CompanyDto(
        UUID id,
        String legalName,
        String tradeName,
        String siren,
        String siret,
        String vatNumber,
        AddressDto address,
        String email,
        String phone,
        String website,
        String iban,
        String bic,
        String rcsOrRm,
        String apeNaf,
        String legalForm
) implements Serializable {}