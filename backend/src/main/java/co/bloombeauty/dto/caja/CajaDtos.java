package co.bloombeauty.dto.caja;

import co.bloombeauty.domain.enums.EstadoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTOs del módulo de caja (HU-02, HU-11) agrupados como records anidados. */
public final class CajaDtos {

    private CajaDtos() {
    }

    public record AbrirCajaRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
            BigDecimal montoInicial) {
    }

    public record CerrarCajaRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal efectivoDeclarado,
            @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal transferenciaDeclarada,
            @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal tarjetaDeclarada,
            @Size(max = 300) String observaciones) {
    }

    public record CajaResponse(
            Long id,
            EstadoCaja estado,
            LocalDateTime fechaApertura,
            LocalDateTime fechaCierre,
            String abiertaPor,
            BigDecimal montoInicial,
            BigDecimal totalVentasEfectivo,
            BigDecimal totalVentasTransferencia,
            BigDecimal totalVentasTarjeta,
            BigDecimal totalGastosEfectivo,
            BigDecimal efectivoEsperado,
            BigDecimal diferencia,
            String observaciones) {
    }
}
