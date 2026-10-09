package com.example.demo.controller;

import com.example.demo.dto.favorito.FavoritoRequestDTO;
import com.example.demo.dto.favorito.FavoritoResponseDTO;
import com.example.demo.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Recurso propio con CRUD completo (persistido en PostgreSQL)")
public class FavoritoController {

    private final FavoritoService service;

    public FavoritoController(FavoritoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un favorito", description = "Crea un favorito y devuelve 201 Created.")
    public FavoritoResponseDTO crear(@Valid @RequestBody FavoritoRequestDTO dto) {
        return service.crear(dto);
    }

    @GetMapping
    @Operation(summary = "Listar favoritos", description = "Devuelve todos los favoritos guardados en PostgreSQL.")
    public List<FavoritoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito por id", description = "Devuelve 404 si el favorito no existe.")
    public FavoritoResponseDTO obtenerPorId(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un favorito", description = "Reemplaza productoId y nota. Devuelve 404 si no existe, 400 si el body es inválido.")
    public FavoritoResponseDTO actualizar(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody FavoritoRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un favorito", description = "Devuelve 204 sin body. 404 si no existe.")
    public void eliminar(
            @Parameter(description = "Id del favorito", example = "1")
            @PathVariable Long id) {
        service.eliminar(id);
    }
}
