package co.bloombeauty.exception;

/** 409 Conflict: la operación choca con el estado actual del recurso. */
public class ConflictoException extends BloomException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
