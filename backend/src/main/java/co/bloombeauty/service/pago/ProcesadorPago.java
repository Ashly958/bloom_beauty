package co.bloombeauty.service.pago;

import co.bloombeauty.domain.MetodoPago;
import co.bloombeauty.domain.PagoVenta;
import co.bloombeauty.domain.enums.TipoMetodoPago;
import co.bloombeauty.dto.venta.PagoRequest;

import java.util.Set;

/**
 * PATRÓN STRATEGY. Cada método de pago tiene reglas distintas (HU-01 RN-4):
 * el efectivo calcula cambio; transferencia y tarjeta exigen referencia.
 * Agregar un método nuevo (p. ej. Nequi) = crear otra implementación,
 * sin modificar VentaServiceImpl (Principio Abierto/Cerrado).
 */
public interface ProcesadorPago {

    /** Métodos de pago que esta estrategia sabe procesar. */
    Set<TipoMetodoPago> soporta();

    /** Valida el pago y construye la entidad PagoVenta (sin asociarla a la venta). */
    PagoVenta procesar(PagoRequest solicitud, MetodoPago metodo);
}
