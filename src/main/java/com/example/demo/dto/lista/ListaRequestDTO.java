package com.example.demo.dto.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que el cliente envía al crear una lista.
 */
@Schema(description = "Datos para crear una lista de favoritos")
public record ListaRequestDTO(
        @Schema(description = "Nombre de la lista", example = "Regalos")
        @NotBlank(message = "nombre no puede estar vacío")
        @Size(max = 100, message = "nombre no puede superar los 100 caracteres")
        String nombre
) {
}
