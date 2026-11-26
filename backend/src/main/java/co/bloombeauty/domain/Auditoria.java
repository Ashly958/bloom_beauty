package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Bitácora de acciones sensibles (ventas, anulaciones, cambios de precio, cierres). */
@Entity
@Table(name = "auditoria",
       indexes = {
           @Index(name = "ix_auditoria_fecha", columnList = "fecha"),
           @Index(name = "ix_auditoria_entidad", columnList = "entidad, entidad_id")
       })
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 40)
    private String accion;

    @Column(nullable = false, length = 40)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String detalle;

    @Column(length = 45)
    private String ip;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
