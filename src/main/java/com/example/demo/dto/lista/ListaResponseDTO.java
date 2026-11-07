package com.example.demo.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lo que la API devuelve para una lista.
 */
@Schema(description = "Lista guardada")
public record ListaResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "Regalos") String nombre,
        @Schema(description = "Id del usuario dueño de la lista", example = "1") Long usuarioId
) {
}
