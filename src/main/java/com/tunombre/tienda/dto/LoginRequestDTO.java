package com.tunombre.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "El usuario o email es obligatorio")
    private String user;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
