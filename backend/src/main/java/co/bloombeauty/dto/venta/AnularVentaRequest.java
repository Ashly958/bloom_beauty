package co.bloombeauty.dto.venta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularVentaRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 10, max = 200, message = "El motivo debe tener entre 10 y 200 caracteres")
        String motivo
) {
}
