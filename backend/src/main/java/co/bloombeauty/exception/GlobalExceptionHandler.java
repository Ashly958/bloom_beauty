package co.bloombeauty.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores. Traduce excepciones a respuestas RFC 7807
 * (application/problem+json) con un campo "codigo" estable para el frontend.
 *
 * <ul>
 *   <li>400 Bad Request ........ Bean Validation de DTOs</li>
 *   <li>401 / 423 .............. credenciales inválidas / cuenta bloqueada</li>
 *   <li>403 Forbidden .......... rol sin permiso (RNF-01)</li>
 *   <li>404 Not Found .......... recurso inexistente</li>
 *   <li>409 Conflict ........... stock agotado, bloqueo optimista, caja cerrada, duplicados</li>
 *   <li>422 Unprocessable ...... reglas de negocio (pago incompleto, descuento excedido)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(fe -> errores.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "VALIDACION", "Datos de entrada inválidos", req);
        pd.setProperty("errores", errores);
        return pd;
    }

    @ExceptionHandler(AutenticacionException.class)
    public ProblemDetail autenticacion(AutenticacionException ex, HttpServletRequest req) {
        return problema(ex.getStatus(), ex.getCodigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return problema(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tienes permisos para realizar esta acción", req);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage(), req);
    }

    /** Dos ventas concurrentes modificaron el mismo producto: @Version detectó el choque (RNF-02). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail bloqueoOptimista(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        log.warn("Conflicto de concurrencia sobre {}", ex.getPersistentClassName());
        return problema(HttpStatus.CONFLICT, "STOCK_AGOTADO",
                "Stock agotado: otro usuario acaba de vender este producto. Actualiza el carrito", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "DATO_DUPLICADO",
                "El registro viola una restricción de unicidad o integridad", req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intenta de nuevo", req);
    }

    private ProblemDetail problema(HttpStatus status, String codigo, String detalle, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("codigo", codigo);
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }
}
