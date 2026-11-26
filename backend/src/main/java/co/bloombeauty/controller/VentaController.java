package co.bloombeauty.controller;

import co.bloombeauty.dto.venta.AnularVentaRequest;
import co.bloombeauty.dto.venta.VentaRequest;
import co.bloombeauty.dto.venta.VentaResponse;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controlador REST delgado: recibe el DTO, delega en el servicio y arma la
 * respuesta HTTP. No contiene reglas de negocio ni accede a repositorios.
 */
@Tag(name = "Ventas (POS)")
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @Operation(summary = "Registrar venta y pago (HU-01)")
    @ApiResponse(responseCode = "201", description = "Venta registrada; devuelve el comprobante")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "409", description = "Stock agotado o caja cerrada")
    @ApiResponse(responseCode = "422", description = "Pago incompleto, descuento no permitido o referencia faltante")
    @PostMapping
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMINISTRADOR')")
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request,
                                                   @AuthenticationPrincipal UsuarioPrincipal usuario) {
        VentaResponse venta = ventaService.registrar(request, usuario);
        return ResponseEntity.created(URI.create("/api/v1/ventas/" + venta.id())).body(venta);
    }

    @Operation(summary = "Consultar comprobante")
    @GetMapping("/{id}")
    public VentaResponse obtener(@PathVariable Long id) {
        return ventaService.obtener(id);
    }

    @Operation(summary = "Anular venta (HU-12) – solo Administrador")
    @PatchMapping("/{id}/anulacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public VentaResponse anular(@PathVariable Long id, @Valid @RequestBody AnularVentaRequest request,
                                @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ventaService.anular(id, request.motivo(), admin);
    }
}
