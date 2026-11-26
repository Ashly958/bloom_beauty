package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra",
       uniqueConstraints = @UniqueConstraint(name = "uq_compra_producto", columnNames = {"compra_id", "producto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    public static DetalleCompra de(Producto producto, int cantidad, BigDecimal costoUnitario) {
        DetalleCompra d = new DetalleCompra();
        d.producto = producto;
        d.cantidad = cantidad;
        d.costoUnitario = costoUnitario;
        d.subtotal = costoUnitario.multiply(BigDecimal.valueOf(cantidad));
        return d;
    }
}
