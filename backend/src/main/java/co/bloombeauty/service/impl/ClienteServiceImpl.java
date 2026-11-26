package co.bloombeauty.service.impl;

import co.bloombeauty.domain.Cliente;
import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteRequest;
import co.bloombeauty.dto.cliente.ClienteDtos.ClienteResponse;
import co.bloombeauty.exception.ConflictoException;
import co.bloombeauty.exception.RecursoNoEncontradoException;
import co.bloombeauty.repository.ClienteRepository;
import co.bloombeauty.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> buscar(String texto, int pagina, int tamano) {
        String t = texto == null ? "" : texto.trim();
        return PageResponse.de(clienteRepository
                .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(t, t,
                        PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 50)))
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento).map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", numeroDocumento));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest r) {
        if (clienteRepository.existsByNumeroDocumento(r.numeroDocumento())) {
            throw new ConflictoException("CLIENTE_DUPLICADO", "Ya existe un cliente con el documento " + r.numeroDocumento());
        }
        Cliente c = new Cliente();
        aplicar(r, c);
        return toResponse(clienteRepository.save(c));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest r) {
        Cliente c = clienteRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
        aplicar(r, c);
        return toResponse(c);
    }

    private void aplicar(ClienteRequest r, Cliente c) {
        c.setTipoDocumento(r.tipoDocumento());
        c.setNumeroDocumento(r.numeroDocumento());
        c.setNombres(r.nombres().trim());
        c.setApellidos(r.apellidos());
        c.setTelefono(r.telefono());
        c.setEmail(r.email());
        c.setFechaNacimiento(r.fechaNacimiento());
        c.setAceptaDatos(r.aceptaDatos());
    }

    private ClienteResponse toResponse(Cliente c) {
        String nombre = (c.getNombres() + " " + (c.getApellidos() == null ? "" : c.getApellidos())).trim();
        return new ClienteResponse(c.getId(), c.getTipoDocumento(), c.getNumeroDocumento(), nombre,
                c.getTelefono(), c.getEmail());
    }
}
