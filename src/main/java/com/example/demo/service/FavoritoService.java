package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoMapper;
import com.example.demo.dto.favorito.FavoritoRequestDTO;
import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica del recurso propio Favoritos (CRUD sobre PostgreSQL vía puerto).
 * No cambió respecto del TP1: sigue hablando contra FavoritoRepository.
 * La única novedad es que valida que la lista exista (404 si no) porque
 * todo favorito pertenece a una lista (referencia por id).
 */
@Service
public class FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listas;

    public FavoritoService(FavoritoRepository repository, ListaRepository listas) {
        this.repository = repository;
        this.listas = listas;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        validarListaExiste(dto.listaId());
        Favorito guardado = repository.save(FavoritoMapper.toEntity(dto));
        return FavoritoMapper.toResponse(guardado);
    }

    public List<FavoritoResponseDTO> listar() {
        return repository.findAll().stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        return FavoritoMapper.toResponse(buscar(id));
    }

    public List<FavoritoResponseDTO> listarPorLista(Long listaId) {
        validarListaExiste(listaId);
        return repository.findByListaId(listaId).stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        validarListaExiste(dto.listaId());
        Favorito existente = buscar(id);
        existente.setProductoId(dto.productoId());
        existente.setNota(dto.nota());
        existente.setListaId(dto.listaId());
        return FavoritoMapper.toResponse(repository.save(existente));
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Favorito no encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private Favorito buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado: " + id));
    }

    private void validarListaExiste(Long listaId) {
        if (!listas.existsById(listaId)) {
            throw new RecursoNoEncontradoException("Lista no encontrada: " + listaId);
        }
    }
}
