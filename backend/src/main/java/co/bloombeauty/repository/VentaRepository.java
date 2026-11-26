package co.bloombeauty.repository;

import co.bloombeauty.domain.Venta;
import co.bloombeauty.domain.enums.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Comprobante completo: carga detalles y productos en una consulta. */
    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "cliente", "usuario"})
    Optional<Venta> findWithDetallesById(Long id);

    Page<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    Page<Venta> findByClienteIdOrderByFechaDesc(Long clienteId, Pageable pageable);

    @Query("""
           SELECT COALESCE(SUM(v.total), 0) FROM Venta v
           WHERE v.estado = :estado AND v.fecha BETWEEN :desde AND :hasta
           """)
    BigDecimal sumarTotal(@Param("estado") EstadoVenta estado,
                          @Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta);

    long countByEstadoAndFechaBetween(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta);
}
