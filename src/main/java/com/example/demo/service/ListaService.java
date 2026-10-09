package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoMapper;
import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.dto.lista.ListaMapper;
import com.example.demo.dto.lista.ListaRequestDTO;
import com.example.demo.dto.lista.ListaResponseDTO;
import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica del recurso propio Listas.
 */
@Service
public class ListaService {

    private final ListaRepository listas;
    private final FavoritoRepository favoritos;

    public ListaService(ListaRepository listas, FavoritoRepository favoritos) {
        this.listas = listas;
        this.favoritos = favoritos;
    }

    public ListaResponseDTO crear(ListaRequestDTO dto) {
        Lista guardada = listas.save(ListaMapper.toDomain(dto));
        return ListaMapper.toResponse(guardada);
    }

    public List<ListaResponseDTO> listar() {
        return listas.findAll().stream().map(ListaMapper::toResponse).toList();
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        return ListaMapper.toResponse(buscar(id));
    }

    public List<FavoritoResponseDTO> favoritosDeLista(Long id) {
        buscar(id);
        return favoritos.findByListaId(id).stream().map(FavoritoMapper::toResponse).toList();
    }

    public void eliminar(Long id) {
        buscar(id);
        if (!favoritos.findByListaId(id).isEmpty()) {
            throw new ListaConFavoritosException(
                    "No se puede eliminar la lista " + id + " porque todavía tiene favoritos");
        }
        listas.deleteById(id);
    }

    /**
     * Mueve todos los favoritos de la lista origen a la destino y elimina la origen.
     * Es atómica: si una escritura falla a mitad de camino, ninguna queda aplicada.
     */
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        Lista origen = buscar(origenId);
        Lista destino = buscar(destinoId);
        if (origen.getId().equals(destino.getId())) {
            return;
        }
        List<Favorito> aMover = favoritos.findByListaId(origen.getId());
        for (Favorito f : aMover) {
            f.setListaId(destino.getId());
            favoritos.save(f);
        }
        listas.deleteById(origen.getId());
    }

    private Lista buscar(Long id) {
        return listas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada: " + id));
    }
}
