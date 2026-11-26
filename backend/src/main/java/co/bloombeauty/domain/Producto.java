package co.bloombeauty.domain;

import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.exception.StockAgotadoException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Producto del catálogo.
 * <ul>
 *   <li>RNF-02: {@code @Version} activa el bloqueo optimista. Si dos trabajadores venden la
 *       última unidad a la vez, el segundo UPDATE no encuentra la versión esperada y
 *       Hibernate lanza OptimisticLockException → 409 "Stock agotado".</li>
 *   <li>RNF-03: {@code @SQLDelete} convierte el DELETE en borrado lógico y
 *       {@code @SQLRestriction} oculta los inactivos en las consultas.</li>
 *   <li>RNF-07: índices para búsqueda por nombre y para la alerta de stock bajo.</li>
 * </ul>
 */
@Entity
@Table(name = "productos",
       indexes = {
           @Index(name = "ix_productos_nombre", columnList = "nombre"),
           @Index(name = "ix_productos_stock", columnList = "stock_actual, stock_minimo"),
           @Index(name = "ix_productos_categoria_marca", columnList = "categoria_id, marca_id, inactivo"),
           @Index(name = "ix_productos_vencimiento", columnList = "fecha_vencimiento")
       })
@SQLDelete(sql = "UPDATE productos SET inactivo = true WHERE id = ? AND version = ?")
@SQLRestriction("inactivo = false")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 20)
    private String codigoBarras;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    @Column(length = 40)
    private String tono;

    @Column(name = "precio_costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "porcentaje_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIva = new BigDecimal("19.00");

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 5;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(nullable = false)
    private boolean inactivo;

    /** Bloqueo optimista (RNF-02). */
    @Version
    @Column(nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Relación N:M con proveedores resuelta con entidad intermedia (tiene atributos propios). */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductoProveedor> proveedores = new HashSet<>();

    // ---------- Métodos de dominio ----------

    /** Descuenta unidades vendidas; falla con 409 si no alcanza (RF-17). */
    public int descontarStock(int cantidad) {
        if (inactivo) {
            throw new ReglaNegocioException("PRODUCTO_NO_DISPONIBLE", "Producto no disponible: " + nombre);
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("CANTIDAD_INVALIDA", "La cantidad debe ser mayor a 0");
        }
        if (cantidad > stockActual) {
            throw new StockAgotadoException(nombre, stockActual, cantidad);
        }
        int anterior = stockActual;
        stockActual -= cantidad;
        return anterior;
    }

    /** Incrementa stock por compra, ajuste positivo o reintegro de anulación. */
    public int incrementarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("CANTIDAD_INVALIDA", "La cantidad debe ser mayor a 0");
        }
        int anterior = stockActual;
        stockActual += cantidad;
        return anterior;
    }

    public boolean tieneStockBajo() {
        return stockActual <= stockMinimo;
    }

    public boolean vencePronto(LocalDate hoy, int dias) {
        return fechaVencimiento != null && !fechaVencimiento.isAfter(hoy.plusDays(dias));
    }

    public void inactivar() {
        this.inactivo = true;
    }
}
