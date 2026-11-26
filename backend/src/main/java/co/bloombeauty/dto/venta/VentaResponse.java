package co.bloombeauty.dto.venta;

import co.bloombeauty.domain.enums.EstadoVenta;
import co.bloombeauty.domain.enums.TipoMetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Comprobante de venta devuelto con 201 Created (HU-01 paso 6). */
public record VentaResponse(
        Long id,
        String numeroFactura,
        LocalDateTime fecha,
        String cliente,
        String vendedor,
        EstadoVenta estado,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal ivaTotal,
        BigDecimal total,
        BigDecimal cambio,
        List<Linea> lineas,
        List<Pago> pagos
) {
    public record Linea(Long productoId, String producto, int cantidad, BigDecimal precioUnitario,
                        BigDecimal porcentajeDescuento, BigDecimal subtotal) {
    }

    public record Pago(TipoMetodoPago metodo, BigDecimal monto, BigDecimal cambio, String referencia) {
    }
}
