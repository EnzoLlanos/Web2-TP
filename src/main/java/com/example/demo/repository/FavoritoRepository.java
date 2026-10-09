package com.example.demo.repository;

import com.example.demo.model.Favorito;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (contrato) del módulo Favoritos. El dominio y el Service solo
 * conocen esta interfaz; las implementaciones concretas (adapters) viven
 * en infraestructura y son intercambiables.
 */
public interface FavoritoRepository {

    Favorito save(Favorito favorito);

    List<Favorito> findAll();

    Optional<Favorito> findById(Long id);

    List<Favorito> findByListaId(Long listaId);

    void deleteById(Long id);

    boolean existsById(Long id);
}
