package co.bloombeauty.repository;

import co.bloombeauty.domain.MetodoPago;
import co.bloombeauty.domain.enums.TipoMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    Optional<MetodoPago> findByNombreAndActivoTrue(TipoMetodoPago nombre);
}
