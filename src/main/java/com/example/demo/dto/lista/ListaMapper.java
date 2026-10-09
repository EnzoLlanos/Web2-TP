package com.example.demo.dto.lista;

import com.example.demo.model.Lista;

/**
 * Mapeo manual entre dominio Lista y sus DTOs.
 */
public final class ListaMapper {

    private ListaMapper() {
    }

    public static Lista toDomain(ListaRequestDTO dto) {
        return new Lista(null, dto.nombre());
    }

    public static ListaResponseDTO toResponse(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
    }
}
