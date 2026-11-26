package co.bloombeauty.repository;

import co.bloombeauty.domain.ProductoProveedor;
import co.bloombeauty.domain.ProductoProveedorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, ProductoProveedorId> {

    List<ProductoProveedor> findByProductoId(Long productoId);
}
