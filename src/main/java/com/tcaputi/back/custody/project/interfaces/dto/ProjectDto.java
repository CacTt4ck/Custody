package com.tcaputi.back.custody.project.interfaces.dto;

import com.tcaputi.back.custody.project.domain.model.BillingMode;
import com.tcaputi.back.custody.project.domain.model.Project;
import com.tcaputi.back.custody.project.domain.model.ProjectStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link Project}
 */
public record ProjectDto(UUID id, UUID clientId, String code, String name, String description, LocalDate startDate, LocalDate endDate,
                         ProjectStatus status, BillingMode billingMode, BigDecimal hourlyRate,
                         String currency) implements Serializable {
}