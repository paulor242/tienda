package com.tunombre.tienda.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token-expiration:600000}")
    private Long tokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, Integer rolId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("rolId", rolId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(userId.toString());
        });
    }

    public Integer extractRolId(String token) {
        return extractClaim(token, claims -> {
            Object rolId = claims.get("rolId");
            if (rolId instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(rolId.toString());
        });
    }

    public String refreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        Long userId = extractUserId(token);
        Integer rolId = extractRolId(token);
        return generateToken(userId, rolId, username);
    }
}
