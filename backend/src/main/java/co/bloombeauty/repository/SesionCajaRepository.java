package co.bloombeauty.repository;

import co.bloombeauty.domain.SesionCaja;
import co.bloombeauty.domain.enums.EstadoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    /** Solo existe una caja ABIERTA a la vez (HU-02 RN-1). */
    Optional<SesionCaja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja estado);

    boolean existsByEstado(EstadoCaja estado);
}
