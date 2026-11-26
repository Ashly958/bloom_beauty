package co.bloombeauty.repository;

import co.bloombeauty.domain.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    boolean existsByProveedorIdAndNumeroFacturaProveedor(Long proveedorId, String numeroFacturaProveedor);
}
