package com.example.demo.exception;

/**
 * Email inexistente o contraseña incorrecta. Mismo mensaje en ambos casos
 * para no filtrar qué parte del login falló.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
