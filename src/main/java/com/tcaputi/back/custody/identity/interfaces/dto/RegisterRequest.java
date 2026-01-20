package com.tcaputi.back.custody.identity.interfaces.dto;

import com.tcaputi.back.custody.identity.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    String username,
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    String email,
    
    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 10, message = "Le mot de passe doit contenir au moins 10 caractères")
    String password,
    
    String firstName,
    
    String lastName,
    
    User.Role role
) {}
