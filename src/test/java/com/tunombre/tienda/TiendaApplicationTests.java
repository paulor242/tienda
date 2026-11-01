package com.tunombre.tienda;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TiendaApplicationTests {

    @Test
    void generateAdminHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encoded = encoder.encode(rawPassword);
        System.out.println("BCRYPT_ADMIN_HASH: " + encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
    }
}
