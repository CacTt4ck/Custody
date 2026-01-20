package com.tcaputi.back.custody.tax.interfaces.dto;

import com.tcaputi.back.custody.tax.domain.model.TaxProfile;
import com.tcaputi.back.custody.tax.domain.model.TaxType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link TaxProfile}
 */
public record TaxProfileDto(UUID id, TaxType type, String label,
                            List<String> mandatoryMentions) implements Serializable {
}