package com.tunombre.tienda.dto;

import com.tunombre.tienda.enums.OrderStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequestDTO {

    @NotNull(message = "El nuevo estado del pedido es obligatorio")
    private OrderStatusEnum status;
}
