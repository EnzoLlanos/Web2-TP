package com.example.demo.dto.favorito;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Lo que la API devuelve. Incluye id generado y fecha de agregado.
 */
@Schema(description = "Favorito guardado")
public record FavoritoResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long productoId,
        @Schema(example = "Regalo para mamá") String nota,
        @Schema(example = "2026-09-17T10:15:30") LocalDateTime fechaAgregado,
        @Schema(description = "Id de la lista a la que pertenece", example = "1") Long listaId
) {
}
