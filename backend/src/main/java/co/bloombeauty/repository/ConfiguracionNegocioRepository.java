package co.bloombeauty.repository;

import co.bloombeauty.domain.ConfiguracionNegocio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConfiguracionNegocioRepository extends JpaRepository<ConfiguracionNegocio, Short> {

    /**
     * SELECT ... FOR UPDATE sobre la única fila de configuración. Serializa la
     * asignación del consecutivo BB-XXXXXX para que nunca se repita (HU-01 RN-5).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConfiguracionNegocio c WHERE c.id = 1")
    Optional<ConfiguracionNegocio> bloquearParaConsecutivo();

    @Query("SELECT c FROM ConfiguracionNegocio c WHERE c.id = 1")
    Optional<ConfiguracionNegocio> obtener();
}
