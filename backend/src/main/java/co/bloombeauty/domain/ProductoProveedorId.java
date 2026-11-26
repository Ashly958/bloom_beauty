package co.bloombeauty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Llave compuesta (producto_id, proveedor_id) de la tabla intermedia N:M. */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductoProveedorId implements Serializable {

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "proveedor_id")
    private Long proveedorId;
}
