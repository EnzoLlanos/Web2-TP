package com.example.demo.security;

import com.example.demo.model.Rol;

/**
 * Contenido útil de un JWT ya validado.
 */
public record TokenPayload(Long usuarioId, String email, Rol rol) {
}
