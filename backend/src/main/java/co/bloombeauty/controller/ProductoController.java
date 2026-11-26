package co.bloombeauty.controller;

import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.producto.ProductoRequest;
import co.bloombeauty.dto.producto.ProductoResponse;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Catálogo de productos")
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Buscar productos paginados (RF-08, RNF-07)")
    @GetMapping
    public PageResponse<ProductoResponse> buscar(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Integer categoriaId,
                                                 @RequestParam(required = false) Integer marcaId,
                                                 @RequestParam(defaultValue = "false") boolean stockBajo,
                                                 @RequestParam(defaultValue = "0") int pagina,
                                                 @RequestParam(defaultValue = "20") int tamano) {
        return productoService.buscar(q, categoriaId, marcaId, stockBajo, pagina, tamano);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @Operation(summary = "Buscar por código de barras (lector del POS)")
    @GetMapping("/codigo/{codigoBarras}")
    public ProductoResponse porCodigo(@PathVariable String codigoBarras) {
        return productoService.obtenerPorCodigo(codigoBarras);
    }

    @Operation(summary = "Registrar producto (HU-05) – solo Administrador; Trabajador recibe 403 (RNF-01)")
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request,
                                                  @AuthenticationPrincipal UsuarioPrincipal admin) {
        ProductoResponse creado = productoService.crear(request, admin);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + creado.id())).body(creado);
    }

    @Operation(summary = "Editar producto y precio (RF-06)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @Operation(summary = "Inactivar producto – borrado lógico (HU-06, RNF-03)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivar(@PathVariable Long id) {
        productoService.inactivar(id);
    }
}
