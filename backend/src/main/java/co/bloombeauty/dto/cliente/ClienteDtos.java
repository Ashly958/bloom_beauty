package co.bloombeauty.dto.cliente;

import co.bloombeauty.domain.enums.TipoDocumentoCliente;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** DTOs del módulo de clientes (HU-10). */
public final class ClienteDtos {

    private ClienteDtos() {
    }

    public record ClienteRequest(
            @NotNull TipoDocumentoCliente tipoDocumento,
            @NotBlank @Pattern(regexp = "[0-9A-Za-z]{5,15}") String numeroDocumento,
            @NotBlank @Size(max = 60) String nombres,
            @Size(max = 60) String apellidos,
            @Pattern(regexp = "3\\d{9}", message = "Celular colombiano de 10 dígitos") String telefono,
            @Email @Size(max = 120) String email,
            @Past LocalDate fechaNacimiento,
            @AssertTrue(message = "Se requiere la autorización de tratamiento de datos (Ley 1581 de 2012)")
            boolean aceptaDatos) {
    }

    public record ClienteResponse(
            Long id,
            TipoDocumentoCliente tipoDocumento,
            String numeroDocumento,
            String nombreCompleto,
            String telefono,
            String email) {
    }
}
