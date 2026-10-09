package com.example.demo.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA de infraestructura (tabla favoritos). Una entidad JPA no puede
 * ser un record: necesita constructor vacío y estado mutable para que
 * Hibernate gestione su ciclo de vida. El record/dominio Favorito no se toca.
 */
@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, length = 280)
    private String nota;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAgregado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaEntity lista;

    protected FavoritoEntity() {
    }

    public FavoritoEntity(Long id, Long productoId, String nota, LocalDateTime fechaAgregado, ListaEntity lista) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAgregado = fechaAgregado;
        this.lista = lista;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    public ListaEntity getLista() {
        return lista;
    }

    public void setLista(ListaEntity lista) {
        this.lista = lista;
    }
}
