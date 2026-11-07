package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta del login: el cliente debe enviar el token en
 * Authorization: Bearer <token> en cada request protegida.
 */
@Schema(description = "Token JWT emitido")
public record AuthResponseDTO(
        @Schema(description = "JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(description = "Esquema de autenticación", example = "Bearer") String tipo,
        @Schema(description = "Vigencia del token en milisegundos", example = "86400000") long expiraEnMs
) {
}
