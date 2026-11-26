package co.bloombeauty.domain;

import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.exception.StockAgotadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas unitarias de las reglas de dominio de HU-01 (sin Spring ni base de datos). */
class VentaDominioTest {

    private Producto labial;

    @BeforeEach
    void setUp() {
        labial = new Producto();
        labial.setId(3L);
        labial.setNombre("Labial Mate Larga Duración");
        labial.setPrecioVenta(new BigDecimal("17500.00"));
        labial.setPorcentajeIva(new BigDecimal("19.00"));
        labial.setStockActual(2);
    }

    @Test
    void calculaTotalesConDescuentoEIvaIncluido() {
        Venta venta = new Venta();
        venta.agregarDetalle(DetalleVenta.de(labial, 2, new BigDecimal("10.00")));

        venta.recalcularTotales();

        assertThat(venta.getSubtotal()).isEqualByComparingTo("35000.00");
        assertThat(venta.getDescuentoTotal()).isEqualByComparingTo("3500.00");
        assertThat(venta.getTotal()).isEqualByComparingTo("31500.00");
        assertThat(venta.getIvaTotal()).isEqualByComparingTo("5029.41"); // 31500 - 31500/1.19
    }

    @Test
    void rechazaPagoIncompletoCon422() {
        Venta venta = new Venta();
        venta.agregarDetalle(DetalleVenta.de(labial, 1, BigDecimal.ZERO));
        venta.recalcularTotales();
        PagoVenta pago = new PagoVenta();
        pago.setMonto(new BigDecimal("10000.00"));
        venta.agregarPago(pago);

        assertThatThrownBy(venta::validarPagosCubrenTotal)
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Faltan $7500.00");
    }

    @Test
    void noPermiteVenderMasDelStockDisponible() {
        assertThatThrownBy(() -> labial.descontarStock(3)).isInstanceOf(StockAgotadoException.class);
        assertThat(labial.getStockActual()).isEqualTo(2);
    }
}
