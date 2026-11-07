package com.example.demo.dto.auth;

import com.example.demo.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos públicos del usuario. No incluye passwordHash.
 */
@Schema(description = "Usuario registrado")
public record UsuarioResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "ana@demo.com") String email,
        @Schema(example = "USER") Rol rol
) {
}
