package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoMapper;
import com.example.demo.dto.favorito.FavoritoRequestDTO;
import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.UsuarioActual;
import com.example.demo.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Lógica del recurso propio Favoritos (CRUD sobre PostgreSQL vía puerto).
 *
 * Un favorito hereda el dueño de su lista. Por eso todas las operaciones
 * validan la propiedad de la lista contenedora acá, en el service, antes
 * de tocar el repositorio (403 si es ajena y el usuario no es ADMIN).
 */
@Service
public class FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listas;
    private final UsuarioActual usuarioActual;

    public FavoritoService(
            FavoritoRepository repository,
            ListaRepository listas,
            UsuarioActual usuarioActual) {
        this.repository = repository;
        this.listas = listas;
        this.usuarioActual = usuarioActual;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO dto) {
        verificarListaPropia(dto.listaId());
        Favorito guardado = repository.save(FavoritoMapper.toEntity(dto));
        return FavoritoMapper.toResponse(guardado);
    }

    public List<FavoritoResponseDTO> listar() {
        UsuarioPrincipal usuario = usuarioActual.get();
        if (usuario.rol() == Rol.ADMIN) {
            return repository.findAll().stream().map(FavoritoMapper::toResponse).toList();
        }

        List<Long> idsPropios = listas.findByUsuarioId(usuario.id()).stream()
                .map(Lista::getId)
                .toList();
        if (idsPropios.isEmpty()) {
            return List.of();
        }
        return repository.findByListaIds(idsPropios).stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = buscar(id);
        verificarListaPropia(favorito.getListaId());
        return FavoritoMapper.toResponse(favorito);
    }

    public List<FavoritoResponseDTO> listarPorLista(Long listaId) {
        verificarListaPropia(listaId);
        return repository.findByListaId(listaId).stream()
                .map(FavoritoMapper::toResponse)
                .toList();
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO dto) {
        Favorito existente = buscar(id);
        verificarListaPropia(existente.getListaId());
        verificarListaPropia(dto.listaId());

        existente.setProductoId(dto.productoId());
        existente.setNota(dto.nota());
        existente.setListaId(dto.listaId());
        return FavoritoMapper.toResponse(repository.save(existente));
    }

    public void eliminar(Long id) {
        Favorito existente = buscar(id);
        verificarListaPropia(existente.getListaId());
        repository.deleteById(id);
    }

    private Favorito buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado: " + id));
    }

    /**
     * 404 si la lista no existe; 403 si existe pero pertenece a otro usuario
     * y el autenticado no es ADMIN.
     */
    private void verificarListaPropia(Long listaId) {
        Lista lista = listas.findById(listaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada: " + listaId));

        UsuarioPrincipal usuario = usuarioActual.get();
        boolean esAdmin = usuario.rol() == Rol.ADMIN;
        if (!esAdmin && !Objects.equals(lista.getUsuarioId(), usuario.id())) {
            throw new AccesoDenegadoException(
                    "No tenés permiso para acceder a la lista " + listaId);
        }
    }
}
