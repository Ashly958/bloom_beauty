package co.bloombeauty.domain.enums;

/**
 * Métodos de pago soportados por el POS. Coincide con los registros semilla de
 * la tabla metodos_pago y sirve de llave para elegir la estrategia de cobro.
 */
public enum TipoMetodoPago {
    EFECTIVO, TRANSFERENCIA, TARJETA_DEBITO, TARJETA_CREDITO
}
