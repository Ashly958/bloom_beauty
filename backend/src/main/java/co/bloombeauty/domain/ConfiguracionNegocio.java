package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Parámetros generales del negocio. Un único registro (id = 1). */
@Entity
@Table(name = "configuracion_negocio")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionNegocio {

    public static final short ID_UNICO = 1;

    @Id
    private Short id;

    @Column(name = "nombre_comercial", nullable = false, length = 100)
    private String nombreComercial;

    @Column(nullable = false, length = 15)
    private String nit;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 10)
    private String telefono;

    @Column(name = "prefijo_factura", nullable = false, length = 5)
    private String prefijoFactura = "BB";

    @Column(name = "consecutivo_actual", nullable = false)
    private int consecutivoActual;

    @Column(name = "descuento_maximo_trabajador", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoMaximoTrabajador = new BigDecimal("10.00");

    @Column(name = "umbral_diferencia_caja", nullable = false, precision = 12, scale = 2)
    private BigDecimal umbralDiferenciaCaja = new BigDecimal("5000.00");

    /** Avanza el consecutivo; se invoca con la fila bloqueada (PESSIMISTIC_WRITE). */
    public int siguienteConsecutivo() {
        return ++consecutivoActual;
    }
}
