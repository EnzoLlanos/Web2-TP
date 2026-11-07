package com.example.demo.repository;

import com.example.demo.model.Usuario;

import java.util.Optional;

/**
 * Puerto (contrato) del módulo Usuarios. Mismo patrón que
 * FavoritoRepository/ListaRepository: el service no conoce JPA.
 */
public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
