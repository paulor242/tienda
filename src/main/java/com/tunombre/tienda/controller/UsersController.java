package com.tunombre.tienda.controller;

import com.tunombre.tienda.dto.*;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.security.RequiresRole;
import com.tunombre.tienda.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/me")
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> getMyProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserResponseDTO profile = usersService.getProfile(userId);
        return ResponseEntity.ok(HttpGlobalResponseDTO.<UserResponseDTO>builder()
                .message("Perfil de usuario obtenido correctamente")
                .data(profile)
                .build());
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = usersService.getAllUsers();
        return ResponseEntity.ok(HttpGlobalResponseDTO.<List<UserResponseDTO>>builder()
                .message("Listado de usuarios obtenido correctamente")
                .data(users)
                .build());
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> createUser(
            @Valid @RequestBody UserCreateRequestDTO dto) {
        UserResponseDTO created = usersService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HttpGlobalResponseDTO.<UserResponseDTO>builder()
                        .message("Usuario creado exitosamente")
                        .data(created)
                        .build());
    }

    @PatchMapping("/{id}/role")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequestDTO dto) {
        UserResponseDTO updated = usersService.updateRole(id, dto.getRoleId());
        return ResponseEntity.ok(HttpGlobalResponseDTO.<UserResponseDTO>builder()
                .message("Rol de usuario actualizado exitosamente")
                .data(updated)
                .build());
    }

    @PatchMapping("/{id}/status")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequestDTO dto) {
        UserResponseDTO updated = usersService.updateStatus(id, dto.getActive());
        return ResponseEntity.ok(HttpGlobalResponseDTO.<UserResponseDTO>builder()
                .message("Estado de usuario actualizado exitosamente")
                .data(updated)
                .build());
    }
}
