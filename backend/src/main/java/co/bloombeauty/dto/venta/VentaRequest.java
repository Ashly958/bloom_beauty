package co.bloombeauty.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cuerpo de POST /api/v1/ventas (HU-01). Coincide campo a campo con el contrato
 * OpenAPI VentaRequest. Las validaciones estructurales son Bean Validation (400);
 * las reglas de negocio se validan en el servicio (409 / 422).
 */
@Schema(description = "Solicitud para registrar una venta en el POS")
public record VentaRequest(

        @Schema(description = "Id de la clienta; null = Consumidor final", example = "15")
        Long clienteId,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Size(max = 100, message = "Máximo 100 líneas por venta")
        List<@Valid LineaVentaRequest> lineas,

        @NotEmpty(message = "Debe registrar al menos un pago")
        @Size(max = 4, message = "Máximo 4 pagos por venta")
        List<@Valid PagoRequest> pagos,

        @Schema(description = "Clave del administrador para autorizar descuentos > 10 %")
        String claveAutorizacion
) {
}
