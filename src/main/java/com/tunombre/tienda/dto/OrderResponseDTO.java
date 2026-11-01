package com.tunombre.tienda.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tunombre.tienda.enums.OrderStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long id;
    private UserResponseDTO client;
    private ProductResponseDTO product;
    private Integer quantity;
    private BigDecimal total;
    private OrderStatusEnum status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
