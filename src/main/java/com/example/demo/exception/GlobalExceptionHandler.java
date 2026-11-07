package com.example.demo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador centralizado de errores de toda la API. En vez de que cada
 * controller arme su propio JSON de error, cualquier excepción que llegue
 * hasta acá se traduce a un mismo formato (ProblemDetail, RFC 7807):
 * { "status", "title", "detail", ... }.
 *
 * Cuando se implemente el controller de favoritos en clase, no hace falta
 * tocar esta clase: alcanza con que FavoritoService lance
 * RecursoNoEncontradoException o que el DTO de entrada tenga anotaciones
 * de Bean Validation para que estos mismos handlers respondan.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ProblemDetail handleEmailDuplicado(EmailDuplicadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("El email ya está registrado");
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridad(DataIntegrityViolationException ex) {
        // Respaldo por si una violación de unicidad (p. ej. email) escapa del service.
        String causa = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        if (causa != null && causa.toLowerCase().contains("email")) {
            ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
            problema.setTitle("El email ya está registrado");
            return problema;
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "La operación viola una restricción de datos");
        problema.setTitle("Conflicto de integridad");
        return problema;
    }

    @ExceptionHandler(ListaConFavoritosException.class)
    public ProblemDetail handleListaConFavoritos(ListaConFavoritosException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("La lista todavía tiene favoritos");
        return problema;
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ProblemDetail handleServicioExterno(ServicioExternoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problema.setTitle("Falla al consumir un servicio externo");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Uno o más campos no son válidos");
        problema.setTitle("Error de validación");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        String detalle = ex.getClass().getSimpleName();
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            detalle += ": " + ex.getMessage();
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, detalle);
        problema.setTitle("Error interno");
        return problema;
    }
}
