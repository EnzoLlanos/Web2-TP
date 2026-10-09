package com.example.demo.dto.favorito;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

/**
 * Lo que el cliente envía al crear/actualizar. Validado con Bean Validation.
 */
@Schema(description = "Datos para crear o actualizar un favorito")
public record FavoritoRequestDTO(
        @Schema(description = "Id del producto en DummyJSON", example = "1")
        @NotNull(message = "productoId es obligatorio")
        @Positive(message = "productoId debe ser mayor a 0")
        Long productoId,

        @Schema(description = "Nota personal", example = "Regalo para mamá")
        @NotBlank(message = "nota no puede estar vacía")
        @Size(max = 280, message = "nota no puede superar los 280 caracteres")
        String nota,

        @Schema(description = "Id de la lista a la que pertenece el favorito", example = "1")
        @NotNull(message = "listaId es obligatorio")
        @Positive(message = "listaId debe ser mayor a 0")
        Long listaId
) {
}
