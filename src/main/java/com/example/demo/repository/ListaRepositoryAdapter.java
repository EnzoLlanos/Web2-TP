package com.example.demo.repository;

import com.example.demo.model.Lista;
import com.example.demo.persistence.ListaJpaRepository;
import com.example.demo.persistence.UsuarioJpaRepository;
import com.example.demo.persistence.entity.ListaEntity;
import com.example.demo.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA detrás del puerto {@link ListaRepository}.
 */
@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpa;
    private final UsuarioJpaRepository usuarios;

    public ListaRepositoryAdapter(ListaJpaRepository jpa, UsuarioJpaRepository usuarios) {
        this.jpa = jpa;
        this.usuarios = usuarios;
    }

    @Override
    public Lista save(Lista lista) {
        UsuarioEntity usuarioRef = usuarios.getReferenceById(lista.getUsuarioId());
        ListaEntity guardada = jpa.save(toEntity(lista, usuarioRef));
        return toDomain(guardada);
    }

    @Override
    public List<Lista> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }

    private ListaEntity toEntity(Lista dominio, UsuarioEntity usuarioRef) {
        return new ListaEntity(dominio.getId(), dominio.getNombre(), usuarioRef);
    }

    private Lista toDomain(ListaEntity entity) {
        return new Lista(
                entity.getId(),
                entity.getNombre(),
                entity.getUsuario() != null ? entity.getUsuario().getId() : null);
    }
}
