package com.example.demo.model;

/**
 * Dominio propio: usuario dueño de sus listas. Nunca guarda la contraseña
 * original, solo el hash producido por PasswordEncoder.
 */
public class Usuario {

    private Long id;
    private String email;
    private String passwordHash;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(Long id, String email, String passwordHash, Rol rol) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
