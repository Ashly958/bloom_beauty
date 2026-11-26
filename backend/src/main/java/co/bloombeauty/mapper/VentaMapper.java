package co.bloombeauty.mapper;

import co.bloombeauty.domain.PagoVenta;
import co.bloombeauty.domain.Venta;
import co.bloombeauty.dto.venta.VentaResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VentaMapper {

    public VentaResponse toResponse(Venta v) {
        BigDecimal cambio = v.getPagos().stream()
                .map(PagoVenta::getCambio)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String cliente = v.getCliente() == null
                ? "Consumidor final"
                : v.getCliente().getNombres() + " " + (v.getCliente().getApellidos() == null ? "" : v.getCliente().getApellidos());
        return new VentaResponse(
                v.getId(), v.getNumeroFactura(), v.getFecha(), cliente.trim(),
                v.getUsuario().getNombreCompleto(), v.getEstado(),
                v.getSubtotal(), v.getDescuentoTotal(), v.getIvaTotal(), v.getTotal(), cambio,
                v.getDetalles().stream().map(d -> new VentaResponse.Linea(
                        d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                        d.getPrecioUnitario(), d.getPorcentajeDescuento(), d.getSubtotal())).toList(),
                v.getPagos().stream().map(p -> new VentaResponse.Pago(
                        p.getMetodoPago().getNombre(), p.getMonto(), p.getCambio(), p.getReferencia())).toList());
    }
}
