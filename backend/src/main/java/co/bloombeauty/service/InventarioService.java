package co.bloombeauty.service;

import co.bloombeauty.domain.Compra;
import co.bloombeauty.domain.MovimientoInventario;
import co.bloombeauty.domain.Producto;
import co.bloombeauty.domain.Usuario;
import co.bloombeauty.domain.Venta;
import co.bloombeauty.domain.enums.TipoMovimiento;
import co.bloombeauty.dto.producto.ProductoResponse;

import java.util.List;

/** Contrato del módulo de inventario (RF-11, RF-12, RF-13). */
public interface InventarioService {

    /** Modifica el stock del producto y registra el movimiento de kárdex correspondiente. */
    MovimientoInventario registrarMovimiento(Producto producto, Usuario usuario, TipoMovimiento tipo,
                                             int cantidad, String motivo, Venta venta, Compra compra);

    List<ProductoResponse> alertas();
}
