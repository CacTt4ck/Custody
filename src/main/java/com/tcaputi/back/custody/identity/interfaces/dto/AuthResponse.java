package com.tcaputi.back.custody.identity.interfaces.dto;

import com.tcaputi.back.custody.identity.domain.model.User;

import java.util.UUID;

public record AuthResponse(
    String token,
    String type,
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    User.Role role
) {
    public AuthResponse(String token, User user) {
        this(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(), 
             user.getFirstName(), user.getLastName(), user.getRole());
    }
}
