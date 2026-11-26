package co.bloombeauty.dto.venta;

import co.bloombeauty.domain.enums.TipoMetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Un pago dentro de un cobro mixto (HU-01 V-3, V-4). */
public record PagoRequest(

        @NotNull(message = "El método de pago es obligatorio")
        TipoMetodoPago metodo,

        @NotNull @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @Digits(integer = 12, fraction = 2)
        BigDecimal monto,

        @Digits(integer = 12, fraction = 2)
        BigDecimal montoRecibido,

        @Size(min = 4, max = 40, message = "La referencia debe tener entre 4 y 40 caracteres")
        String referencia
) {
}
