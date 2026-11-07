package com.example.demo.service;

import com.example.demo.dto.auth.AuthResponseDTO;
import com.example.demo.dto.auth.LoginRequestDTO;
import com.example.demo.dto.auth.RegistroRequestDTO;
import com.example.demo.dto.auth.UsuarioResponseDTO;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.exception.EmailDuplicadoException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Registro y login. La contraseña en texto plano solo existe en memoria
 * durante el request: se hashea antes de guardar y nunca se devuelve.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        String email = normalizar(dto.email());
        if (usuarios.existsByEmail(email)) {
            throw new EmailDuplicadoException(email);
        }

        Usuario usuario = new Usuario(
                null,
                email,
                passwordEncoder.encode(dto.password()),
                Rol.USER);
        try {
            Usuario guardado = usuarios.save(usuario);
            return new UsuarioResponseDTO(guardado.getId(), guardado.getEmail(), guardado.getRol());
        } catch (DataIntegrityViolationException ex) {
            // Carrera entre dos registros concurrentes: la unique de email es
            // la última defensa y también debe responder 409, no 500.
            throw new EmailDuplicadoException(email);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String email = normalizar(dto.email());
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return new AuthResponseDTO(
                jwtService.generar(usuario),
                "Bearer",
                jwtService.getExpirationMs());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
