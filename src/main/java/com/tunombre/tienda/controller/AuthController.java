package com.tunombre.tienda.controller;

import com.tunombre.tienda.dto.*;
import com.tunombre.tienda.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO user = authService.register(dto);
        HttpGlobalResponseDTO<UserResponseDTO> response = HttpGlobalResponseDTO.<UserResponseDTO>builder()
                .message("Usuario registrado exitosamente con rol CLIENTE")
                .data(user)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponseDTO<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO authResponse = authService.login(dto);
        HttpGlobalResponseDTO<AuthResponseDTO> response = HttpGlobalResponseDTO.<AuthResponseDTO>builder()
                .message("Autenticación exitosa")
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<HttpGlobalResponseDTO<RefreshTokenResponseDTO>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        RefreshTokenResponseDTO refreshed = authService.refreshToken(authHeader);
        HttpGlobalResponseDTO<RefreshTokenResponseDTO> response = HttpGlobalResponseDTO.<RefreshTokenResponseDTO>builder()
                .message("Token renovado exitosamente")
                .data(refreshed)
                .build();
        return ResponseEntity.ok(response);
    }
}
