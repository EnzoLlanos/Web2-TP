package com.example.demo.repository;

import com.example.demo.model.Lista;
import com.example.demo.persistence.ListaJpaRepository;
import com.example.demo.persistence.entity.ListaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA detrás del puerto {@link ListaRepository}.
 */
@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpa;

    public ListaRepositoryAdapter(ListaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity guardada = jpa.save(toEntity(lista));
        return toDomain(guardada);
    }

    @Override
    public List<Lista> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
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

    private ListaEntity toEntity(Lista dominio) {
        return new ListaEntity(dominio.getId(), dominio.getNombre());
    }

    private Lista toDomain(ListaEntity entity) {
        return new Lista(entity.getId(), entity.getNombre());
    }
}
