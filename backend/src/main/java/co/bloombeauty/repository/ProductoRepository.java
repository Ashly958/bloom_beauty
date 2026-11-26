package co.bloombeauty.repository;

import co.bloombeauty.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Patrón Repository: abstrae el acceso a datos de productos (Spring Data JPA). */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Búsqueda paginada del catálogo y del POS (RF-08, RNF-07). Usa ix_productos_nombre
     * e ix_productos_categoria_marca. El EntityGraph evita el problema N+1 al cargar
     * categoría y marca, que por defecto son LAZY.
     */
    @EntityGraph(attributePaths = {"categoria", "marca"})
    @Query("""
           SELECT p FROM Producto p
           WHERE (:texto IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                  OR p.codigoBarras = :texto)
             AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
             AND (:marcaId IS NULL OR p.marca.id = :marcaId)
             AND (:soloStockBajo = false OR p.stockActual <= p.stockMinimo)
           """)
    Page<Producto> buscar(@Param("texto") String texto,
                          @Param("categoriaId") Integer categoriaId,
                          @Param("marcaId") Integer marcaId,
                          @Param("soloStockBajo") boolean soloStockBajo,
                          Pageable pageable);

    /** Alertas de inventario (RF-13): stock ≤ mínimo o vencimiento dentro del límite. */
    @Query("""
           SELECT p FROM Producto p
           WHERE p.stockActual <= p.stockMinimo
              OR (p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento <= :limite)
           ORDER BY p.stockActual ASC
           """)
    List<Producto> findAlertas(@Param("limite") LocalDate limite);

    /** Carga varios productos de una venta con una sola consulta. */
    List<Producto> findByIdIn(List<Long> ids);
}
