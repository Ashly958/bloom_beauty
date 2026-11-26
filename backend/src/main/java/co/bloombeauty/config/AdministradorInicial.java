package co.bloombeauty.config;

import co.bloombeauty.domain.Rol;
import co.bloombeauty.domain.Usuario;
import co.bloombeauty.domain.enums.TipoDocumentoUsuario;
import co.bloombeauty.repository.RolRepository;
import co.bloombeauty.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer Administrador al arrancar si no existe ninguno. La contraseña
 * llega por variable de entorno (nunca queda escrita en el código ni en el SQL)
 * y se guarda cifrada con BCrypt (RNF-06).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministradorInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bloom.admin.email:}")
    private String email;

    @Value("${bloom.admin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank() || usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Rol admin = rolRepository.findByNombre(Rol.ADMINISTRADOR)
                .orElseThrow(() -> new IllegalStateException("Falta el rol ADMINISTRADOR en la tabla roles"));
        Usuario u = new Usuario();
        u.setRol(admin);
        u.setTipoDocumento(TipoDocumentoUsuario.CC);
        u.setNumeroDocumento("0000000001");
        u.setNombres("Administrador");
        u.setApellidos("Bloom Beauty");
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        usuarioRepository.save(u);
        log.info("Administrador inicial creado: {}", email);
    }
}
