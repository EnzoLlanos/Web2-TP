package com.example.demo.dto.favorito;

import com.example.demo.model.Favorito;

/**
 * Mapeo manual entre entidad y DTOs (a elección del TP: a mano).
 */
public final class FavoritoMapper {

    private FavoritoMapper() {
    }

    public static Favorito toEntity(FavoritoRequestDTO dto) {
        return new Favorito(null, dto.productoId(), dto.nota(), null, dto.listaId());
    }

    public static FavoritoResponseDTO toResponse(Favorito entity) {
        return new FavoritoResponseDTO(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAgregado(),
                entity.getListaId()
        );
    }
}
