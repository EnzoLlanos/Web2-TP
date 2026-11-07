package com.example.demo.repository;

import com.example.demo.model.Usuario;
import com.example.demo.persistence.UsuarioJpaRepository;
import com.example.demo.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter JPA detrás del puerto {@link UsuarioRepository}.
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity guardado = jpa.save(toEntity(usuario));
        return toDomain(guardado);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private UsuarioEntity toEntity(Usuario dominio) {
        return new UsuarioEntity(
                dominio.getId(),
                dominio.getEmail(),
                dominio.getPasswordHash(),
                dominio.getRol());
    }

    private Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRol());
    }
}
