package co.bloombeauty.exception;

/** 409: no hay caja ABIERTA para vender, o la caja ya fue cerrada/abierta por otro usuario. */
public class CajaNoDisponibleException extends ConflictoException {

    public CajaNoDisponibleException(String mensaje) {
        super("CAJA_NO_DISPONIBLE", mensaje);
    }
}
