package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad intermedia de la relación N:M productos ↔ proveedores. Se modela como
 * entidad (y no con @ManyToMany) porque guarda atributos propios: costo de
 * referencia y si es el proveedor principal.
 */
@Entity
@Table(name = "producto_proveedor")
@Getter
@Setter
@NoArgsConstructor
public class ProductoProveedor {

    @EmbeddedId
    private ProductoProveedorId id = new ProductoProveedorId();

    @MapsId("productoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @MapsId("proveedorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "costo_referencia", precision = 12, scale = 2)
    private BigDecimal costoReferencia;

    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal;
}
