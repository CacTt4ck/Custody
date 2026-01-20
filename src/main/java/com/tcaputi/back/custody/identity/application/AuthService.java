package com.tcaputi.back.custody.identity.application;

import com.tcaputi.back.custody.identity.domain.model.User;
import com.tcaputi.back.custody.identity.infrastructure.UserRepository;
import com.tcaputi.back.custody.identity.interfaces.dto.AuthResponse;
import com.tcaputi.back.custody.identity.interfaces.dto.LoginRequest;
import com.tcaputi.back.custody.identity.interfaces.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'enregistrement pour l'utilisateur: {}", request.username());
        
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
        }
        
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Cet email existe déjà");
        }
        
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role() != null ? request.role() : User.Role.USER)
                .active(true)
                .build();
        
        user = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", user.getUsername());
        
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'utilisateur: {}", request.username());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        log.info("Connexion réussie pour l'utilisateur: {}", user.getUsername());
        return new AuthResponse(token, user);
    }
}
