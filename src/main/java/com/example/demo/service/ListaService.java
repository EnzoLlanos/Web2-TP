package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoMapper;
import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.dto.lista.ListaMapper;
import com.example.demo.dto.lista.ListaRequestDTO;
import com.example.demo.dto.lista.ListaResponseDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaConFavoritosException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.UsuarioActual;
import com.example.demo.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Lógica del recurso propio Listas.
 *
 * La autorización por propiedad vive acá (y no en el Controller) porque:
 *  - todo acceso a una lista pasa por este service, así ningún endpoint
 *    futuro puede olvidarse de validar;
 *  - el controller queda enfocado en HTTP/DTOs y el service en reglas;
 *  - se testea con un test unitario sin levantar Spring MVC.
 * El dueño de una lista creada sale SIEMPRE del token (UsuarioActual),
 * nunca de un campo del body.
 */
@Service
public class ListaService {

    private final ListaRepository listas;
    private final FavoritoRepository favoritos;
    private final UsuarioActual usuarioActual;

    public ListaService(
            ListaRepository listas,
            FavoritoRepository favoritos,
            UsuarioActual usuarioActual) {
        this.listas = listas;
        this.favoritos = favoritos;
        this.usuarioActual = usuarioActual;
    }

    public ListaResponseDTO crear(ListaRequestDTO dto) {
        UsuarioPrincipal usuario = usuarioActual.get();
        Lista guardada = listas.save(ListaMapper.toDomain(dto, usuario.id()));
        return ListaMapper.toResponse(guardada);
    }

    public List<ListaResponseDTO> listar() {
        UsuarioPrincipal usuario = usuarioActual.get();
        List<Lista> visibles = usuario.rol() == Rol.ADMIN
                ? listas.findAll()
                : listas.findByUsuarioId(usuario.id());
        return visibles.stream().map(ListaMapper::toResponse).toList();
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        return ListaMapper.toResponse(buscarPropia(id));
    }

    public List<FavoritoResponseDTO> favoritosDeLista(Long id) {
        buscarPropia(id);
        return favoritos.findByListaId(id).stream().map(FavoritoMapper::toResponse).toList();
    }

    public void eliminar(Long id) {
        buscarPropia(id);
        if (!favoritos.findByListaId(id).isEmpty()) {
            throw new ListaConFavoritosException(
                    "No se puede eliminar la lista " + id + " porque todavía tiene favoritos");
        }
        listas.deleteById(id);
    }

    /**
     * Mueve todos los favoritos de la lista origen a la destino y elimina la origen.
     * Es atómica: si una escritura falla a mitad de camino, ninguna queda aplicada.
     * Además valida que el usuario sea dueño de ambas listas (o ADMIN).
     */
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        Lista origen = buscarPropia(origenId);
        Lista destino = buscarPropia(destinoId);
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

    /**
     * Busca la lista (404 si no existe) y valida propiedad (403 si es ajena
     * y el usuario no es ADMIN). No se devuelve 404 para listas ajenas:
     * "no existe" y "no es tuya" son casos distintos.
     */
    private Lista buscarPropia(Long id) {
        Lista lista = listas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista no encontrada: " + id));

        UsuarioPrincipal usuario = usuarioActual.get();
        boolean esAdmin = usuario.rol() == Rol.ADMIN;
        if (!esAdmin && !Objects.equals(lista.getUsuarioId(), usuario.id())) {
            throw new AccesoDenegadoException(
                    "No tenés permiso para acceder a la lista " + id);
        }
        return lista;
    }
}
