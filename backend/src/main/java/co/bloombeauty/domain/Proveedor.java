package co.bloombeauty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String nit;

    @Column(name = "razon_social", nullable = false, length = 120)
    private String razonSocial;

    @Column(name = "nombre_contacto", length = 80)
    private String nombreContacto;

    @Column(nullable = false, length = 10)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(length = 150)
    private String direccion;

    @Column(length = 60)
    private String ciudad;

    @Column(nullable = false)
    private boolean inactivo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Lado inverso de la relación N:M con productos (tabla producto_proveedor). */
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private Set<ProductoProveedor> productos = new HashSet<>();
}
