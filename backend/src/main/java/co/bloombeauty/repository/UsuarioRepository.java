package co.bloombeauty.repository;

import co.bloombeauty.domain.Usuario;
import co.bloombeauty.domain.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El rol se necesita en cada login para construir las authorities del JWT. */
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    /** Administradores activos: se usan para validar la clave de autorización de descuentos. */
    List<Usuario> findByRol_NombreAndEstado(String rol, EstadoUsuario estado);
}
