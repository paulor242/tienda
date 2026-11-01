package com.tunombre.tienda.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN(1),
    CLIENTE(2),
    VENDEDOR(3);

    private final int id;

    RoleEnum(int id) {
        this.id = id;
    }

    public static RoleEnum fromId(int id) {
        for (RoleEnum role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol inválido con id: " + id);
    }
}
