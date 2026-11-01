package com.tunombre.tienda.exception;

import com.tunombre.tienda.dto.HttpGlobalResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        HttpGlobalResponseDTO<Map<String, String>> response = HttpGlobalResponseDTO.<Map<String, String>>builder()
                .message("Error de validación en los campos enviados")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpGlobalResponseDTO.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(HttpGlobalResponseDTO.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(HttpGlobalResponseDTO.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(HttpGlobalResponseDTO.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(HttpGlobalResponseDTO.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpGlobalResponseDTO<Object>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HttpGlobalResponseDTO.builder()
                        .message("Error interno en el servidor: " + ex.getMessage())
                        .data(null)
                        .build());
    }
}
