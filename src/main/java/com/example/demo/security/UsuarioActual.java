package com.example.demo.security;

import com.example.demo.exception.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Punto único para que los services obtengan al usuario autenticado.
 * Así la regla de propiedad no depende de que cada controller recuerde
 * pasar el id: el service siempre lo lee de la identidad del request.
 */
@Component
public class UsuarioActual {

    public UsuarioPrincipal get() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccesoDenegadoException("No hay un usuario autenticado en el request");
        }
        return principal;
    }
}
