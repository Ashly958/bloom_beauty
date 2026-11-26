package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Pago de una venta; varias filas por venta permiten el pago mixto (RF-16). */
@Entity
@Table(name = "pagos_venta")
@Getter
@Setter
@NoArgsConstructor
public class PagoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_recibido", precision = 14, scale = 2)
    private BigDecimal montoRecibido;

    @Column(precision = 14, scale = 2)
    private BigDecimal cambio = BigDecimal.ZERO;

    @Column(length = 40)
    private String referencia;
}
