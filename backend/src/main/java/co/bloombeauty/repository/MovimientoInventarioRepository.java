package co.bloombeauty.repository;

import co.bloombeauty.domain.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /** Kárdex de un producto (RF-12); usa el índice compuesto ix_mov_producto_fecha. */
    Page<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId, Pageable pageable);
}
