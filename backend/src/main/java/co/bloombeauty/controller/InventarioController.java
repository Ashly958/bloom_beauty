package co.bloombeauty.controller;

import co.bloombeauty.dto.producto.ProductoResponse;
import co.bloombeauty.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Inventario")
@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @Operation(summary = "Productos con stock bajo o próximos a vencer (RF-13)")
    @GetMapping("/alertas")
    public List<ProductoResponse> alertas() {
        return inventarioService.alertas();
    }
}
