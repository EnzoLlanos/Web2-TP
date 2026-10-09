package com.example.demo.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Body de POST /api/listas/{origenId}/mover-favoritos: la lista destino.
 */
@Schema(description = "Destino para mover todos los favoritos de una lista a otra")
public record MoverFavoritosRequestDTO(
        @Schema(description = "Id de la lista destino", example = "2")
        @NotNull(message = "destinoId es obligatorio")
        @Positive(message = "destinoId debe ser mayor a 0")
        Long destinoId
) {
}
