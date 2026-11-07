package com.example.demo.security;

import com.example.demo.model.Rol;

/**
 * Identidad que el filtro JWT deja disponible durante el request.
 * Es lo mínimo que necesitan los services para autorizar: id, email y rol.
 */
public record UsuarioPrincipal(Long id, String email, Rol rol) {
}
