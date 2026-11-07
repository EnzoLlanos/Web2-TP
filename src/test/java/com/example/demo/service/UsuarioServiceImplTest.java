package com.example.demo.service;

import com.example.demo.dto.auth.LoginRequestDTO;
import com.example.demo.dto.auth.RegistroRequestDTO;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String PASSWORD_PLANO = "secreta123";
    private static final String HASH_BCRYPT = "$2a$10$hash-simulado-para-test";

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService service;

    @Test
    void registrar_nuncaGuardaLaPasswordEnTextoPlano() {
        when(usuarios.existsByEmail("ana@demo.com")).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD_PLANO)).thenReturn(HASH_BCRYPT);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario aGuardar = invocacion.getArgument(0);
            return new Usuario(1L, aGuardar.getEmail(), aGuardar.getPasswordHash(), aGuardar.getRol());
        });

        service.registrar(new RegistroRequestDTO("ana@demo.com", PASSWORD_PLANO));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertThat(guardado.getPasswordHash())
                .as("la contraseña original no debe persistirse")
                .isNotEqualTo(PASSWORD_PLANO)
                .isEqualTo(HASH_BCRYPT);
        assertThat(guardado.getRol()).isEqualTo(Rol.USER);
        verify(passwordEncoder).encode(PASSWORD_PLANO);
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidas() {
        Usuario guardado = new Usuario(1L, "ana@demo.com", HASH_BCRYPT, Rol.USER);
        when(usuarios.findByEmail("ana@demo.com")).thenReturn(Optional.of(guardado));
        when(passwordEncoder.matches("otra", HASH_BCRYPT)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequestDTO("ana@demo.com", "otra")));
        verify(jwtService, never()).generar(any());
    }
}
