package co.bloombeauty.service;

import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteRequest;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteResponse;

public interface ClienteService {

    PageResponse<ClienteResponse> buscar(String texto, int pagina, int tamano);

    ClienteResponse obtenerPorDocumento(String numeroDocumento);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);
}
