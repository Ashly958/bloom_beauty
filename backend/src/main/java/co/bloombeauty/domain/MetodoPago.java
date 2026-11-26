package co.bloombeauty.domain;

import co.bloombeauty.domain.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catálogo de medios de pago. El nombre coincide con {@link TipoMetodoPago}. */
@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
@NoArgsConstructor
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private TipoMetodoPago nombre;

    @Column(name = "requiere_referencia", nullable = false)
    private boolean requiereReferencia;

    @Column(nullable = false)
    private boolean activo = true;
}
