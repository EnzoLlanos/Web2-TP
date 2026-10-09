package com.example.demo.persistence;

import com.example.demo.persistence.entity.FavoritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Interfaz que Spring Data implementa sola. No es el puerto del dominio:
 * es un detalle de infraestructura usado puertas adentro del adapter.
 *
 * Se evita un @OneToMany bidireccional en ListaEntity; el lado inverso
 * (favoritos de una lista) se resuelve con esta consulta derivada.
 */
public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {

    List<FavoritoEntity> findByListaId(Long listaId);
}
