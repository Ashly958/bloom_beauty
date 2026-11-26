package co.bloombeauty.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** DTOs de autenticación (HU-03). */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(max = 64) String password) {
    }

    public record LoginResponse(
            String token,
            Instant expiraEn,
            Long usuarioId,
            String nombre,
            String rol,
            String rutaInicio) {
    }

    public record RecuperarRequest(@NotBlank @Email @Size(max = 120) String email) {
    }

    public record RestablecerRequest(
            @NotBlank @Size(min = 64, max = 64) String token,
            @NotBlank
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
                     message = "La contraseña debe tener 8-64 caracteres con mayúscula, minúscula y número")
            String nuevaPassword,
            @NotBlank String confirmacion) {
    }
}
