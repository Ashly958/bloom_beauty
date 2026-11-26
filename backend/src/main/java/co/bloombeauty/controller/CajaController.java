package co.bloombeauty.controller;

import co.bloombeauty.dto.caja.CajaDtos.AbrirCajaRequest;
import co.bloombeauty.dto.caja.CajaDtos.CajaResponse;
import co.bloombeauty.dto.caja.CajaDtos.CerrarCajaRequest;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.CajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Caja")
@RestController
@RequestMapping("/api/v1/cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @Operation(summary = "Abrir caja del día (RF-20)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CajaResponse abrir(@Valid @RequestBody AbrirCajaRequest request,
                              @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return cajaService.abrir(request, usuario);
    }

    @Operation(summary = "Consultar la caja abierta y sus totales")
    @GetMapping("/actual")
    public CajaResponse actual() {
        return cajaService.actual();
    }

    @Operation(summary = "Arqueo y cierre (HU-02) – solo Administrador")
    @PostMapping("/actual/cierre")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CajaResponse cerrar(@Valid @RequestBody CerrarCajaRequest request,
                               @AuthenticationPrincipal UsuarioPrincipal admin) {
        return cajaService.cerrar(request, admin);
    }
}
