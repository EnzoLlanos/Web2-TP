package com.example.demo.persistence;

import com.example.demo.persistence.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz que Spring Data implementa sola para listas.
 */
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}
