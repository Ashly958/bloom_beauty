package co.bloombeauty.dto.producto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Alta / edición de producto (HU-05). Campos alineados con la tabla productos. */
public record ProductoRequest(

        @NotBlank @Pattern(regexp = "\\d{8,20}", message = "El código de barras debe tener 8 a 20 dígitos")
        String codigoBarras,

        @NotBlank @Size(min = 3, max = 100)
        String nombre,

        @Size(max = 500)
        String descripcion,

        @NotNull Integer categoriaId,

        @NotNull Integer marcaId,

        @Size(max = 40)
        String tono,

        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
        BigDecimal precioCosto,

        @NotNull @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal precioVenta,

        @NotNull @DecimalMin("0.00") @DecimalMax("100.00")
        BigDecimal porcentajeIva,

        @NotNull @Min(0)
        Integer stockActual,

        @NotNull @Min(0)
        Integer stockMinimo,

        @Future(message = "La fecha de vencimiento debe ser posterior a hoy")
        LocalDate fechaVencimiento,

        @Size(max = 255)
        String imagenUrl,

        @Schema(description = "Versión leída por el cliente; se usa para detectar ediciones concurrentes")
        Integer version
) {
}
