package co.bloombeauty.controller;

import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteRequest;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteResponse;
import co.bloombeauty.service.ClienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public PageResponse<ClienteResponse> buscar(@RequestParam(required = false) String q,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "20") int tamano) {
        return clienteService.buscar(q, pagina, tamano);
    }

    @GetMapping("/documento/{numero}")
    public ClienteResponse porDocumento(@PathVariable String numero) {
        return clienteService.obtenerPorDocumento(numero);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return clienteService.crear(request);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }
}
