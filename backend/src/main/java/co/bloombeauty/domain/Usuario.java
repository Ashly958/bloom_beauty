package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.EstadoUsuario;
import co.bloombeauty.domain.enums.TipoDocumentoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

/** Persona que ingresa al sistema (Administrador o Trabajador). */
@Entity
@Table(name = "usuarios",
       indexes = @Index(name = "ix_usuarios_rol_estado", columnList = "rol_id, estado"))
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    public static final int MAX_INTENTOS = 5;
    public static final Duration TIEMPO_BLOQUEO = Duration.ofMinutes(15);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 3)
    private TipoDocumentoUsuario tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 15)
    private String numeroDocumento;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(nullable = false, length = 60)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 10)
    private String telefono;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- Métodos de dominio (RF-02, HU-03) ----------

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
    }

    public long minutosRestantesBloqueo(LocalDateTime ahora) {
        return estaBloqueado(ahora) ? Math.max(1, Duration.between(ahora, bloqueadoHasta).toMinutes()) : 0;
    }

    /** Suma un intento fallido y bloquea 15 min al llegar a 5 (RF-02). */
    public void registrarIntentoFallido(LocalDateTime ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(TIEMPO_BLOQUEO);
            intentosFallidos = 0;
        }
    }

    public void registrarAccesoExitoso(LocalDateTime ahora) {
        intentosFallidos = 0;
        bloqueadoHasta = null;
        ultimoAcceso = ahora;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
