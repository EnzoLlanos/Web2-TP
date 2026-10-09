package com.example.demo.repository;

import com.example.demo.model.Favorito;
import com.example.demo.persistence.FavoritoJpaRepository;
import com.example.demo.persistence.ListaJpaRepository;
import com.example.demo.persistence.entity.FavoritoEntity;
import com.example.demo.persistence.entity.ListaEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA detrás del puerto {@link FavoritoRepository}.
 * Convierte FavoritoEntity ↔ Favorito y delega en Spring Data.
 */
@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpa;
    private final ListaJpaRepository listas;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpa, ListaJpaRepository listas) {
        this.jpa = jpa;
        this.listas = listas;
    }

    @Override
    public Favorito save(Favorito favorito) {
        ListaEntity listaRef = listas.getReferenceById(favorito.getListaId());
        FavoritoEntity entity = toEntity(favorito, listaRef);
        FavoritoEntity guardada = jpa.save(entity);
        return toDomain(guardada);
    }

    @Override
    public List<Favorito> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return jpa.findByListaId(listaId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }

    private FavoritoEntity toEntity(Favorito dominio, ListaEntity listaRef) {
        LocalDateTime fecha = dominio.getFechaAgregado() != null
                ? dominio.getFechaAgregado()
                : LocalDateTime.now();
        return new FavoritoEntity(
                dominio.getId(),
                dominio.getProductoId(),
                dominio.getNota(),
                fecha,
                listaRef);
    }

    private Favorito toDomain(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAgregado(),
                entity.getLista() != null ? entity.getLista().getId() : null);
    }
}
