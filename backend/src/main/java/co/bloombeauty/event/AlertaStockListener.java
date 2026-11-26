package co.bloombeauty.event;

import co.bloombeauty.domain.Producto;
import co.bloombeauty.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Segundo observador del mismo evento: revisa si los productos vendidos quedaron
 * en stock bajo (RF-13). Es asíncrono para no alargar el tiempo de respuesta del
 * POS (RNF-02: &lt; 500 ms). Agregar más reacciones a una venta no toca VentaService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaStockListener {

    private final ProductoRepository productoRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void revisarStock(VentaRegistradaEvent e) {
        List<Producto> productos = productoRepository.findByIdIn(e.productoIds());
        productos.stream()
                .filter(Producto::tieneStockBajo)
                .forEach(p -> log.warn("ALERTA_STOCK producto={} stock={} minimo={}",
                        p.getNombre(), p.getStockActual(), p.getStockMinimo()));
    }
}
