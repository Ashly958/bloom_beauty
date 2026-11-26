package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.EstadoCaja;
import co.bloombeauty.domain.enums.TipoMetodoPago;
import co.bloombeauty.exception.CajaNoDisponibleException;
import co.bloombeauty.exception.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Turno de caja: apertura (RF-20), arqueo y cierre (RF-21, HU-02). */
@Entity
@Table(name = "sesiones_caja",
       indexes = @Index(name = "ix_caja_estado_apertura", columnList = "estado, fecha_apertura"))
@Getter
@Setter
@NoArgsConstructor
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_apertura_id", nullable = false)
    private Usuario usuarioApertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "total_ventas_efectivo", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalVentasEfectivo = BigDecimal.ZERO;

    @Column(name = "total_ventas_transferencia", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalVentasTransferencia = BigDecimal.ZERO;

    @Column(name = "total_ventas_tarjeta", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalVentasTarjeta = BigDecimal.ZERO;

    @Column(name = "total_gastos_efectivo", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalGastosEfectivo = BigDecimal.ZERO;

    @Column(name = "efectivo_esperado", precision = 14, scale = 2)
    private BigDecimal efectivoEsperado;

    @Column(name = "efectivo_declarado", precision = 14, scale = 2)
    private BigDecimal efectivoDeclarado;

    @Column(name = "transferencia_declarada", precision = 14, scale = 2)
    private BigDecimal transferenciaDeclarada;

    @Column(name = "tarjeta_declarada", precision = 14, scale = 2)
    private BigDecimal tarjetaDeclarada;

    @Column(precision = 14, scale = 2)
    private BigDecimal diferencia;

    @Column(length = 300)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    /** Evita que dos administradores cierren la misma caja a la vez (HU-02 E-3). */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // ---------- Fábrica y métodos de dominio ----------

    public static SesionCaja abrir(Usuario usuario, BigDecimal montoInicial, LocalDateTime ahora) {
        if (montoInicial == null || montoInicial.signum() < 0) {
            throw new ReglaNegocioException("MONTO_INVALIDO", "El monto base debe ser mayor o igual a 0");
        }
        SesionCaja s = new SesionCaja();
        s.usuarioApertura = usuario;
        s.montoInicial = montoInicial;
        s.fechaApertura = ahora;
        return s;
    }

    public boolean estaAbierta() {
        return estado == EstadoCaja.ABIERTA;
    }

    public void validarAbierta() {
        if (!estaAbierta()) {
            throw new CajaNoDisponibleException("La caja está cerrada. Abre una caja para vender");
        }
    }

    /** Acumula un pago en el total del método correspondiente. */
    public void registrarIngreso(TipoMetodoPago metodo, BigDecimal monto) {
        validarAbierta();
        switch (metodo) {
            case EFECTIVO -> totalVentasEfectivo = totalVentasEfectivo.add(monto);
            case TRANSFERENCIA -> totalVentasTransferencia = totalVentasTransferencia.add(monto);
            case TARJETA_DEBITO, TARJETA_CREDITO -> totalVentasTarjeta = totalVentasTarjeta.add(monto);
        }
    }

    /** Revierte un pago al anular una venta (las ANULADAS no suman, HU-02 RN-2). */
    public void revertirIngreso(TipoMetodoPago metodo, BigDecimal monto) {
        registrarIngreso(metodo, monto.negate());
    }

    public void registrarGastoEfectivo(BigDecimal monto) {
        validarAbierta();
        totalGastosEfectivo = totalGastosEfectivo.add(monto);
    }

    public BigDecimal calcularEfectivoEsperado() {
        return montoInicial.add(totalVentasEfectivo).subtract(totalGastosEfectivo);
    }

    /**
     * Arqueo y cierre (HU-02). Si |diferencia| supera el umbral configurado, la
     * observación es obligatoria (RN-3) → 422.
     */
    public void cerrar(Usuario admin, BigDecimal efectivo, BigDecimal transferencia, BigDecimal tarjeta,
                       String observaciones, BigDecimal umbral, LocalDateTime ahora) {
        if (!estaAbierta()) {
            throw new CajaNoDisponibleException("La caja ya fue cerrada");
        }
        BigDecimal esperado = calcularEfectivoEsperado();
        BigDecimal dif = efectivo.subtract(esperado)
                .add(transferencia.subtract(totalVentasTransferencia))
                .add(tarjeta.subtract(totalVentasTarjeta));
        if (dif.abs().compareTo(umbral) > 0 && (observaciones == null || observaciones.isBlank())) {
            throw new ReglaNegocioException("OBSERVACION_REQUERIDA",
                    "La diferencia supera $" + umbral.toPlainString() + "; la observación es obligatoria");
        }
        this.efectivoEsperado = esperado;
        this.efectivoDeclarado = efectivo;
        this.transferenciaDeclarada = transferencia;
        this.tarjetaDeclarada = tarjeta;
        this.diferencia = dif;
        this.observaciones = observaciones;
        this.usuarioCierre = admin;
        this.fechaCierre = ahora;
        this.estado = EstadoCaja.CERRADA;
    }
}
