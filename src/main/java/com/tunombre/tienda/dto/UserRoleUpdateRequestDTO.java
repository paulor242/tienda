package com.tunombre.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequestDTO {

    @NotNull(message = "El nuevo id de rol es obligatorio")
    private Integer roleId;
}
