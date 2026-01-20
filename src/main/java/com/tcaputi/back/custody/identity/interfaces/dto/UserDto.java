package com.tcaputi.back.custody.identity.interfaces.dto;

import com.tcaputi.back.custody.identity.domain.model.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
public record UserDto(UUID id, String username, String email, String password, String firstName, String lastName,
                      User.Role role, boolean active, LocalDateTime createdAt,
                      LocalDateTime updatedAt) implements Serializable {
}