package com.example.demo.exception;

/**
 * Se lanza al intentar borrar una lista que todavía tiene favoritos.
 * Se traduce a 409 Conflict (no es un 500: es una regla de negocio).
 */
public class ListaConFavoritosException extends RuntimeException {
    public ListaConFavoritosException(String mensaje) {
        super(mensaje);
    }
}
