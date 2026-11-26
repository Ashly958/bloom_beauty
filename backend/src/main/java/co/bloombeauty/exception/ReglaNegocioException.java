package co.bloombeauty.exception;

/**
 * 422 Unprocessable Entity: la petición está bien formada (pasa Bean Validation)
 * pero viola una regla de negocio (p. ej. suma de pagos distinta al total).
 */
public class ReglaNegocioException extends BloomException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
