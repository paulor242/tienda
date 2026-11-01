package com.tunombre.tienda.security;

import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.exception.ForbiddenException;
import com.tunombre.tienda.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Buscar anotación primero en el método, luego a nivel de clase (Reto 3)
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }

        if (requiresRole == null) {
            return true;
        }

        Object rolIdAttr = request.getAttribute("rolId");
        if (rolIdAttr == null) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        int userRolId;
        if (rolIdAttr instanceof Number number) {
            userRolId = number.intValue();
        } else {
            userRolId = Integer.parseInt(rolIdAttr.toString());
        }

        RoleEnum[] allowedRoles = requiresRole.value();
        boolean hasPermission = false;
        for (RoleEnum role : allowedRoles) {
            if (role.getId() == userRolId) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new ForbiddenException("Acceso denegado: no posees el rol requerido para realizar esta acción");
        }

        return true;
    }
}
