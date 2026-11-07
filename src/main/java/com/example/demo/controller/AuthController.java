package com.example.demo.controller;

import com.example.demo.dto.auth.AuthResponseDTO;
import com.example.demo.dto.auth.LoginRequestDTO;
import com.example.demo.dto.auth.RegistroRequestDTO;
import com.example.demo.dto.auth.UsuarioResponseDTO;
import com.example.demo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro y login. Devuelve un JWT para usar como Bearer token.")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un usuario",
            description = "Crea un usuario con rol USER y contraseña hasheada. Devuelve 409 si el email ya existe.")
    public UsuarioResponseDTO registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        return service.registrar(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida email y contraseña contra el hash guardado. Devuelve 200 con un JWT o 401 si las credenciales son inválidas.")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return service.login(dto);
    }
}
