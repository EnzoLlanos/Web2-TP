package com.example.demo.controller;

import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.dto.lista.ListaRequestDTO;
import com.example.demo.dto.lista.ListaResponseDTO;
import com.example.demo.dto.lista.MoverFavoritosRequestDTO;
import com.example.demo.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Listas propias para organizar favoritos (persistidas en PostgreSQL)")
public class ListaController {

    private final ListaService service;

    public ListaController(ListaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una lista", description = "Crea una lista (p. ej. Regalos) y devuelve 201 Created.")
    public ListaResponseDTO crear(@Valid @RequestBody ListaRequestDTO dto) {
        return service.crear(dto);
    }

    @GetMapping
    @Operation(summary = "Listar listas", description = "Devuelve todas las listas guardadas en PostgreSQL.")
    public List<ListaResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista por id", description = "Devuelve 404 si la lista no existe.")
    public ListaResponseDTO obtenerPorId(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Devuelve los favoritos que pertenecen a la lista. 404 si la lista no existe.")
    public List<FavoritoResponseDTO> favoritosDeLista(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable Long id) {
        return service.favoritosDeLista(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una lista vacía", description = "Devuelve 204 sin body. 404 si no existe, 409 si todavía tiene favoritos.")
    public void eliminar(
            @Parameter(description = "Id de la lista", example = "1")
            @PathVariable Long id) {
        service.eliminar(id);
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mover favoritos entre listas", description = "Reasigna todos los favoritos de la lista origen a la destino y elimina la origen, de forma atómica. 404 si alguna no existe.")
    public void moverFavoritos(
            @Parameter(description = "Id de la lista origen (se elimina)", example = "1")
            @PathVariable Long origenId,
            @Valid @RequestBody MoverFavoritosRequestDTO dto) {
        service.moverFavoritos(origenId, dto.destinoId());
    }
}
