package co.bloombeauty.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evento de dominio publicado al registrar una venta (PATRÓN OBSERVER).
 * Lleva solo datos inmutables, nunca entidades JPA, para que los oyentes
 * no dependan de una sesión de Hibernate abierta.
 */
public record VentaRegistradaEvent(Long ventaId, String numeroFactura, Long usuarioId,
                                   BigDecimal total, List<Long> productoIds) {
}
