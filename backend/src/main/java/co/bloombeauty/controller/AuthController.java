package co.bloombeauty.controller;

import co.bloombeauty.dto.auth.AuthDtos.LoginRequest;
import co.bloombeauty.dto.auth.AuthDtos.LoginResponse;
import co.bloombeauty.dto.auth.AuthDtos.RecuperarRequest;
import co.bloombeauty.dto.auth.AuthDtos.RestablecerRequest;
import co.bloombeauty.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/recuperacion")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void solicitarRecuperacion(@Valid @RequestBody RecuperarRequest request) {
        authService.solicitarRecuperacion(request.email());
    }

    @PostMapping("/restablecimiento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@Valid @RequestBody RestablecerRequest request) {
        authService.restablecer(request);
    }
}
