package com.tunombre.tienda.service;

import com.tunombre.tienda.dto.UserCreateRequestDTO;
import com.tunombre.tienda.dto.UserResponseDTO;
import com.tunombre.tienda.entity.Roles;
import com.tunombre.tienda.entity.Users;
import com.tunombre.tienda.exception.ConflictException;
import com.tunombre.tienda.exception.ResourceNotFoundException;
import com.tunombre.tienda.repository.RolesRepository;
import com.tunombre.tienda.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO getProfile(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        return AuthService.toUserResponseDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return usersRepository.findAll().stream()
                .map(AuthService::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserCreateRequestDTO dto) {
        if (usersRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + dto.getUsername() + "' ya está registrado");
        }
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("El correo electrónico '" + dto.getEmail() + "' ya está registrado");
        }

        Roles role = rolesRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + dto.getRoleId()));

        Users user = Users.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .active(true)
                .rol(role)
                .build();

        Users savedUser = usersRepository.save(user);
        return AuthService.toUserResponseDTO(savedUser);
    }

    public UserResponseDTO updateRole(Long userId, Integer newRoleId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Roles newRole = rolesRepository.findById(newRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + newRoleId));

        user.setRol(newRole);
        Users updated = usersRepository.save(user);
        return AuthService.toUserResponseDTO(updated);
    }

    public UserResponseDTO updateStatus(Long userId, boolean active) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        user.setActive(active);
        Users updated = usersRepository.save(user);
        return AuthService.toUserResponseDTO(updated);
    }
}
