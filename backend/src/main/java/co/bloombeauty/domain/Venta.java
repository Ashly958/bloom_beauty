package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.EstadoVenta;
import co.bloombeauty.exception.ConflictoException;
import co.bloombeauty.exception.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Encabezado de la factura de venta del POS (HU-01, HU-12). Raíz del agregado Venta. */
@Entity
@Table(name = "ventas",
       indexes = {
           @Index(name = "ix_ventas_fecha", columnList = "fecha"),
           @Index(name = "ix_ventas_caja_estado", columnList = "sesion_caja_id, estado"),
           @Index(name = "ix_ventas_cliente_fecha", columnList = "cliente_id, fecha")
       })
@Getter
@Setter
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false, unique = true, length = 20)
    private String numeroFactura;

    /** Null = "Consumidor final" (HU-01 RN-5). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCaja sesionCaja;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "iva_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal ivaTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private EstadoVenta estado = EstadoVenta.PAGADA;

    @Column(name = "motivo_anulacion", length = 200)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulada_por")
    private Usuario anuladaPor;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PagoVenta> pagos = new ArrayList<>();

    // ---------- Métodos de dominio ----------

    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    public void agregarPago(PagoVenta pago) {
        pago.setVenta(this);
        pagos.add(pago);
    }

    /** Recalcula subtotal, descuento, IVA incluido y total a partir de las líneas (HU-01 RN-2). */
    public void recalcularTotales() {
        if (detalles.isEmpty()) {
            throw new ReglaNegocioException("VENTA_SIN_PRODUCTOS", "La venta debe tener al menos un producto");
        }
        subtotal = detalles.stream().map(DetalleVenta::getValorBruto).reduce(BigDecimal.ZERO, BigDecimal::add);
        descuentoTotal = detalles.stream().map(DetalleVenta::getValorDescuento).reduce(BigDecimal.ZERO, BigDecimal::add);
        ivaTotal = detalles.stream().map(DetalleVenta::getIvaIncluido).reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal.subtract(descuentoTotal);
    }

    public BigDecimal totalPagado() {
        return pagos.stream().map(PagoVenta::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** La suma de los pagos debe ser exactamente igual al total (HU-01 RN-4) → 422. */
    public void validarPagosCubrenTotal() {
        BigDecimal pagado = totalPagado();
        int cmp = pagado.compareTo(total);
        if (cmp < 0) {
            throw new ReglaNegocioException("PAGO_INCOMPLETO",
                    "Faltan $" + total.subtract(pagado).toPlainString() + " por cubrir");
        }
        if (cmp > 0) {
            throw new ReglaNegocioException("PAGO_EXCEDIDO",
                    "La suma de los pagos supera el total de la venta");
        }
    }

    /** Anulación con motivo (HU-12). Solo ventas PAGADAS de la caja abierta. */
    public void anular(String motivo, Usuario admin, LocalDateTime ahora) {
        if (estado == EstadoVenta.ANULADA) {
            throw new ConflictoException("VENTA_YA_ANULADA", "La venta " + numeroFactura + " ya fue anulada");
        }
        sesionCaja.validarAbierta();
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("MOTIVO_REQUERIDO", "El motivo de anulación es obligatorio");
        }
        this.estado = EstadoVenta.ANULADA;
        this.motivoAnulacion = motivo;
        this.anuladaPor = admin;
        this.fechaAnulacion = ahora;
    }
}
