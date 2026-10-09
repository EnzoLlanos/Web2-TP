package com.example.demo.model;

/**
 * Dominio propio: una lista organiza favoritos (p. ej. "Regalos").
 * Solo id + nombre; no navega la colección de favoritos.
 */
public class Lista {

    private Long id;
    private String nombre;

    public Lista() {
    }

    public Lista(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
