package com.example.demo.exception;

/**
 * Ya existe un usuario registrado con el email recibido.
 */
public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un usuario con el email " + email);
    }
}
