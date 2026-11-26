package co.bloombeauty.service.impl;

import co.bloombeauty.domain.TokenRecuperacion;
import co.bloombeauty.domain.Usuario;
import co.bloombeauty.dto.auth.AuthDtos.LoginRequest;
import co.bloombeauty.dto.auth.AuthDtos.LoginResponse;
import co.bloombeauty.dto.auth.AuthDtos.RestablecerRequest;
import co.bloombeauty.exception.AutenticacionException;
import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.repository.TokenRecuperacionRepository;
import co.bloombeauty.repository.UsuarioRepository;
import co.bloombeauty.security.JwtService;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.AuthService;
import co.bloombeauty.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;

/** Inicio de sesión con bloqueo por intentos (RF-01, RF-02) y recuperación de contraseña (RF-03). */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificacionService notificacionService;
    private final Clock clock;

    @Value("${bloom.frontend.url:http://localhost:5173}")
    private String urlFrontend;

    /**
     * noRollbackFor: el intento fallido debe quedar guardado aunque se lance la
     * excepción de credenciales inválidas.
     */
    @Override
    @Transactional(noRollbackFor = AutenticacionException.class)
    public LoginResponse login(LoginRequest r) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        Usuario u = usuarioRepository.findByEmailIgnoreCase(r.email().trim())
                .orElseThrow(AutenticacionException::credencialesInvalidas);

        if (!u.estaActivo()) {
            throw AutenticacionException.cuentaInactiva();
        }
        if (u.estaBloqueado(ahora)) {
            throw AutenticacionException.cuentaBloqueada(u.minutosRestantesBloqueo(ahora));
        }
        if (!passwordEncoder.matches(r.password(), u.getPasswordHash())) {
            u.registrarIntentoFallido(ahora);
            throw AutenticacionException.credencialesInvalidas();
        }
        u.registrarAccesoExitoso(ahora);

        var principal = new UsuarioPrincipal(u.getId(), u.getEmail(), u.getRol().getNombre());
        var token = jwtService.emitir(principal);
        String ruta = u.getRol().esAdministrador() ? "/panel" : "/pos";
        return new LoginResponse(token.token(), token.expiraEn(), u.getId(), u.getNombreCompleto(),
                u.getRol().getNombre(), ruta);
    }

    @Override
    @Transactional
    public void solicitarRecuperacion(String email) {
        usuarioRepository.findByEmailIgnoreCase(email.trim()).filter(Usuario::estaActivo).ifPresent(u -> {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            TokenRecuperacion t = new TokenRecuperacion();
            t.setUsuario(u);
            t.setToken(HexFormat.of().formatHex(bytes)); // 64 caracteres hex
            t.setExpiraEn(LocalDateTime.now(clock).plusMinutes(30));
            tokenRepository.save(t);
            notificacionService.enviarEnlaceRecuperacion(u.getEmail(), u.getNombres(),
                    urlFrontend + "/restablecer?token=" + t.getToken());
        });
    }

    @Override
    @Transactional
    public void restablecer(RestablecerRequest r) {
        if (!r.nuevaPassword().equals(r.confirmacion())) {
            throw new ReglaNegocioException("CONFIRMACION_NO_COINCIDE", "La confirmación no coincide con la contraseña");
        }
        TokenRecuperacion t = tokenRepository.findByToken(r.token())
                .filter(tk -> tk.esValido(LocalDateTime.now(clock)))
                .orElseThrow(() -> new ReglaNegocioException("ENLACE_INVALIDO", "El enlace no es válido o ha expirado"));
        t.getUsuario().setPasswordHash(passwordEncoder.encode(r.nuevaPassword()));
        t.marcarUsado();
    }
}
