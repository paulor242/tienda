package com.tunombre.tienda.service;

import com.tunombre.tienda.dto.*;
import com.tunombre.tienda.entity.Roles;
import com.tunombre.tienda.entity.Users;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.exception.ConflictException;
import com.tunombre.tienda.exception.ResourceNotFoundException;
import com.tunombre.tienda.exception.UnauthorizedException;
import com.tunombre.tienda.repository.RolesRepository;
import com.tunombre.tienda.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDTO register(RegisterRequestDTO dto) {
        if (usersRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + dto.getUsername() + "' ya está registrado");
        }
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("El correo electrónico '" + dto.getEmail() + "' ya está registrado");
        }

        Roles clientRole = rolesRepository.findById(RoleEnum.CLIENTE.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no encontrado en el sistema"));

        Users user = Users.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .active(true)
                .rol(clientRole)
                .build();

        Users savedUser = usersRepository.save(user);
        return toUserResponseDTO(savedUser);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Users user = usersRepository.findByUsername(dto.getUser())
                .or(() -> usersRepository.findByEmail(dto.getUser()))
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas o usuario inactivo"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Credenciales inválidas o usuario inactivo");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas o usuario inactivo");
        }

        String token = jwtService.generateToken(user.getId(), user.getRol().getId(), user.getUsername());

        return AuthResponseDTO.builder()
                .token(token)
                .user(toUserResponseDTO(user))
                .build();
    }

    public RefreshTokenResponseDTO refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Encabezado de autorización ausente o con formato incorrecto");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Token inválido o expirado");
        }

        String newToken = jwtService.refreshToken(token);
        return RefreshTokenResponseDTO.builder()
                .token(newToken)
                .build();
    }

    public static UserResponseDTO toUserResponseDTO(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isActive())
                .roleId(user.getRol().getId())
                .role(user.getRol().getName())
                .build();
    }
}
