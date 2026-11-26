package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Línea de producto de una venta. El precio unitario ya incluye IVA (HU-01 RN-2). */
@Entity
@Table(name = "detalle_venta",
       uniqueConstraints = @UniqueConstraint(name = "uq_venta_producto", columnNames = {"venta_id", "producto_id"}),
       indexes = @Index(name = "ix_detalle_venta_producto", columnList = "producto_id"))
@Getter
@Setter
@NoArgsConstructor
public class DetalleVenta {

    private static final BigDecimal CIEN = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    /** % de IVA del producto al momento de la venta; no se persiste en esta tabla. */
    @Transient
    private BigDecimal porcentajeIva;

    public static DetalleVenta de(Producto producto, int cantidad, BigDecimal porcentajeDescuento) {
        DetalleVenta d = new DetalleVenta();
        d.producto = producto;
        d.cantidad = cantidad;
        d.precioUnitario = producto.getPrecioVenta();
        d.porcentajeDescuento = porcentajeDescuento == null ? BigDecimal.ZERO : porcentajeDescuento;
        d.porcentajeIva = producto.getPorcentajeIva();
        d.subtotal = d.getValorBruto().subtract(d.getValorDescuento());
        return d;
    }

    public BigDecimal getValorBruto() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getValorDescuento() {
        return getValorBruto().multiply(porcentajeDescuento).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    /** IVA contenido en el subtotal: subtotal − subtotal / (1 + iva%). */
    public BigDecimal getIvaIncluido() {
        BigDecimal iva = porcentajeIva != null ? porcentajeIva : producto.getPorcentajeIva();
        BigDecimal factor = BigDecimal.ONE.add(iva.divide(CIEN, 4, RoundingMode.HALF_UP));
        BigDecimal base = subtotal.divide(factor, 2, RoundingMode.HALF_UP);
        return subtotal.subtract(base);
    }
}
