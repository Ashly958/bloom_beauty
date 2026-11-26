package co.bloombeauty.exception;

import lombok.Getter;

/**
 * Excepción base del negocio. Cada subclase representa una familia de error y el
 * {@link GlobalExceptionHandler} la traduce a un código HTTP. El dominio y los
 * servicios lanzan excepciones semánticas sin conocer HTTP (SRP / DIP).
 */
@Getter
public abstract class BloomException extends RuntimeException {

    /** Código corto y estable que el frontend usa para mostrar el mensaje correcto. */
    private final String codigo;

    protected BloomException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
