package co.bloombeauty.event;

import java.math.BigDecimal;

public record VentaAnuladaEvent(Long ventaId, String numeroFactura, Long adminId, BigDecimal total, String motivo) {
}
