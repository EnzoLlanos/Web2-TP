package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Dominio propio: un favorito pertenece a una lista (referenciada por id,
 * sin navegar objetos completos). Sin anotaciones JPA: la persistencia
 * vive en los adapters de infraestructura.
 */
public class Favorito {

    private Long id;
    private Long productoId;
    private String nota;
    private LocalDateTime fechaAgregado;
    private Long listaId;

    public Favorito() {
    }

    public Favorito(Long id, Long productoId, String nota, LocalDateTime fechaAgregado, Long listaId) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAgregado = fechaAgregado;
        this.listaId = listaId;
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

    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }
}
