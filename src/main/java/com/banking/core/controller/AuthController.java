package com.banking.core.controller;

import com.banking.core.domain.User;
import com.banking.core.dto.AuthResponse;
import com.banking.core.dto.LoginRequest;
import com.banking.core.dto.RegisterRequest;
import com.banking.core.security.JwtTokenProvider;
import com.banking.core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * POST /api/auth/register
     * Registers a new user and creates a default savings account.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(null)
                .userId(user.getId())
                .username(user.getUsername())
                .message("Registration successful. Please login.")
                .build());
    }

    /**
     * POST /api/auth/login
     * Authenticates user and returns JWT token with user information.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        // Get user details to include userId in response
        User user = userService.findByUsername(request.getUsername());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(request.getUsername())
                .message("Login successful")
                .build());
    }
}
