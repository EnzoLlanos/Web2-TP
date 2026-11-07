package com.example.demo.repository;

import com.example.demo.model.Lista;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (contrato) del módulo Listas. Misma forma que FavoritoRepository.
 */
public interface ListaRepository {

    Lista save(Lista lista);

    List<Lista> findAll();

    List<Lista> findByUsuarioId(Long usuarioId);

    Optional<Lista> findById(Long id);

    void deleteById(Long id);

    boolean existsById(Long id);
}
