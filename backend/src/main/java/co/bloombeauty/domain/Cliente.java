package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.TipoDocumentoCliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Clienta de la tienda. acepta_datos es obligatorio por la Ley 1581 de 2012 (RNF-10). */
@Entity
@Table(name = "clientes",
       indexes = @Index(name = "ix_clientes_nombres", columnList = "nombres, apellidos"))
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 3)
    private TipoDocumentoCliente tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 15)
    private String numeroDocumento;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(length = 60)
    private String apellidos;

    @Column(length = 10)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "acepta_datos", nullable = false)
    private boolean aceptaDatos;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
