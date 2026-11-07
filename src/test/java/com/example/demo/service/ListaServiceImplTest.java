package com.example.demo.service;

import com.example.demo.dto.lista.ListaRequestDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.UsuarioActual;
import com.example.demo.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias puras (sin Spring, sin base): el service con sus
 * colaboradores mockeados.
 */
@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    private static final UsuarioPrincipal ANA =
            new UsuarioPrincipal(42L, "ana@demo.com", Rol.USER);

    @Mock
    private ListaRepository listas;

    @Mock
    private FavoritoRepository favoritos;

    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private ListaService service;

    @Test
    void crear_asociaListaAlUsuarioAutenticado() {
        when(usuarioActual.get()).thenReturn(ANA);
        when(listas.save(any(Lista.class))).thenAnswer(invocacion -> {
            Lista aGuardar = invocacion.getArgument(0);
            return new Lista(1L, aGuardar.getNombre(), aGuardar.getUsuarioId());
        });

        service.crear(new ListaRequestDTO("Regalos"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listas).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Regalos");
        assertThat(captor.getValue().getUsuarioId())
                .as("el dueño debe salir del usuario autenticado, no del body")
                .isEqualTo(ANA.id());
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        when(usuarioActual.get()).thenReturn(ANA);
        when(listas.findById(7L)).thenReturn(Optional.of(
                new Lista(7L, "De otro usuario", 99L)));

        assertThrows(AccesoDenegadoException.class, () -> service.obtenerPorId(7L));
        verify(listas, never()).deleteById(any());
    }
}
