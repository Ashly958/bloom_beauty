package co.bloombeauty.domain.enums;

/** Tipos de movimiento del kárdex (movimientos_inventario.tipo). */
public enum TipoMovimiento {
    ENTRADA_COMPRA,
    SALIDA_VENTA,
    AJUSTE_POSITIVO,
    AJUSTE_NEGATIVO,
    REINTEGRO_ANULACION;

    /** Indica si el movimiento incrementa el stock. */
    public boolean esEntrada() {
        return this == ENTRADA_COMPRA || this == AJUSTE_POSITIVO || this == REINTEGRO_ANULACION;
    }
}
