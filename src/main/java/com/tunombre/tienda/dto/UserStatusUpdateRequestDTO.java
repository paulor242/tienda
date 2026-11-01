package com.tunombre.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequestDTO {

    @NotNull(message = "El estado active es obligatorio")
    private Boolean active;
}
