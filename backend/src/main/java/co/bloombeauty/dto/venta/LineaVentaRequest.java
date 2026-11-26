package co.bloombeauty.dto.venta;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Línea del carrito (HU-01 V-1, V-2). */
public record LineaVentaRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull @Min(value = 1, message = "La cantidad mínima es 1")
        Integer cantidad,

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "30.00", message = "El descuento máximo es 30 %")
        @Digits(integer = 3, fraction = 2)
        BigDecimal porcentajeDescuento
) {
}
