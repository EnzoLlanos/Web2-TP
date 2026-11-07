package com.example.demo.exception;

/**
 * El recurso existe pero el usuario autenticado no es su dueño ni ADMIN.
 * Se traduce a 403 Forbidden (no a 404: "no es tuya" es distinto de "no existe").
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
