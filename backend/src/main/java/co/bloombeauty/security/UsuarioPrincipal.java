package co.bloombeauty.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Identidad del usuario autenticado, reconstruida desde el JWT en cada petición
 * (sesión stateless). Los controladores la reciben con @AuthenticationPrincipal.
 */
public record UsuarioPrincipal(Long id, String email, String rol) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public boolean esAdministrador() {
        return "ADMINISTRADOR".equals(rol);
    }
}
