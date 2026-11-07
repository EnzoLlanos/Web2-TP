package com.example.demo.persistence;

import com.example.demo.persistence.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Interfaz que Spring Data implementa sola para listas.
 * findByUsuarioId navega la relación ManyToOne (usuario.id) sin @OneToMany.
 */
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {

    List<ListaEntity> findByUsuarioId(Long usuarioId);
}
