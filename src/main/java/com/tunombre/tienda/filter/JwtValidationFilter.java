package com.tunombre.tienda.filter;

import com.tunombre.tienda.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Todo /api/v1/tienda/auth/** es público para el filtro
        if (path.contains("/auth/")) {
            return true;
        }

        // 2. GET /products y GET /products/{id} (numérico) son catálogo público
        // Importante: GET /products/mine NO es público y requiere token
        if ("GET".equalsIgnoreCase(method)) {
            if (path.endsWith("/products") || path.endsWith("/products/")) {
                return true;
            }
            if (path.matches(".*\\/products\\/\\d+\\/?$")) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Token de autorización ausente o formato inválido");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty() || !jwtService.isTokenValid(token)) {
            sendUnauthorizedResponse(response, "Token inválido o expirado");
            return;
        }

        try {
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            Integer rolId = jwtService.extractRolId(token);

            // Guardar atributos en el request para controladores e interceptores
            request.setAttribute("username", username);
            request.setAttribute("userId", userId);
            request.setAttribute("rolId", rolId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "Error al procesar el token: " + e.getMessage());
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = "{\"message\":\"" + message + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}
