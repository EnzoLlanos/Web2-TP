package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrarse. La contraseña viaja solo en el request y se
 * persiste hasheada; nunca se devuelve.
 */
@Schema(description = "Datos para registrar un usuario")
public record RegistroRequestDTO(
        @Schema(description = "Email único del usuario", example = "ana@demo.com")
        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe tener un formato válido")
        @Size(max = 255, message = "email no puede superar los 255 caracteres")
        String email,

        @Schema(description = "Contraseña en texto plano (se hashea antes de guardar)", example = "secreta123")
        @NotBlank(message = "password es obligatorio")
        @Size(min = 8, max = 72, message = "password debe tener entre 8 y 72 caracteres")
        String password
) {
}
