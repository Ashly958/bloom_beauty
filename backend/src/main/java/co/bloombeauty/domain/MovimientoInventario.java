package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Kárdex: toda variación de stock queda registrada (RF-12). Registro inmutable. */
@Entity
@Table(name = "movimientos_inventario",
       indexes = @Index(name = "ix_mov_producto_fecha", columnList = "producto_id, fecha"))
@Getter
@Setter
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, updatable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false, updatable = false)
    private int cantidad;

    @Column(name = "stock_anterior", nullable = false, updatable = false)
    private int stockAnterior;

    @Column(name = "stock_resultante", nullable = false, updatable = false)
    private int stockResultante;

    @Column(length = 200, updatable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", updatable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", updatable = false)
    private Compra compra;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /** Fábrica: crea el movimiento a partir del producto ya modificado. */
    public static MovimientoInventario registrar(Producto producto, Usuario usuario, TipoMovimiento tipo,
                                                 int cantidad, int stockAnterior, String motivo,
                                                 LocalDateTime fecha) {
        MovimientoInventario m = new MovimientoInventario();
        m.producto = producto;
        m.usuario = usuario;
        m.tipo = tipo;
        m.cantidad = cantidad;
        m.stockAnterior = stockAnterior;
        m.stockResultante = producto.getStockActual();
        m.motivo = motivo;
        m.fecha = fecha;
        return m;
    }
}
