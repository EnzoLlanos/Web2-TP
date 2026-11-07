package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales para obtener un JWT.
 */
@Schema(description = "Credenciales de login")
public record LoginRequestDTO(
        @Schema(description = "Email registrado", example = "ana@demo.com")
        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe tener un formato válido")
        String email,

        @Schema(description = "Contraseña", example = "secreta123")
        @NotBlank(message = "password es obligatorio")
        String password
) {
}
