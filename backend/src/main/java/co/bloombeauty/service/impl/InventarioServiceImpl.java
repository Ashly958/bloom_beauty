package co.bloombeauty.service.impl;

import co.bloombeauty.domain.Compra;
import co.bloombeauty.domain.MovimientoInventario;
import co.bloombeauty.domain.Producto;
import co.bloombeauty.domain.Usuario;
import co.bloombeauty.domain.Venta;
import co.bloombeauty.domain.enums.TipoMovimiento;
import co.bloombeauty.dto.producto.ProductoResponse;
import co.bloombeauty.mapper.ProductoMapper;
import co.bloombeauty.repository.MovimientoInventarioRepository;
import co.bloombeauty.repository.ProductoRepository;
import co.bloombeauty.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private static final int DIAS_ALERTA_VENCIMIENTO = 30;

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final Clock clock;

    /** Debe ejecutarse dentro de la transacción del caso de uso que lo invoca. */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public MovimientoInventario registrarMovimiento(Producto producto, Usuario usuario, TipoMovimiento tipo,
                                                    int cantidad, String motivo, Venta venta, Compra compra) {
        int stockAnterior = tipo.esEntrada()
                ? producto.incrementarStock(cantidad)
                : producto.descontarStock(cantidad);
        MovimientoInventario mov = MovimientoInventario.registrar(producto, usuario, tipo, cantidad,
                stockAnterior, motivo, LocalDateTime.now(clock));
        mov.setVenta(venta);
        mov.setCompra(compra);
        return movimientoRepository.save(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> alertas() {
        LocalDate limite = LocalDate.now(clock).plusDays(DIAS_ALERTA_VENCIMIENTO);
        return productoRepository.findAlertas(limite).stream().map(productoMapper::toResponse).toList();
    }
}
