package co.bloombeauty.repository;

import co.bloombeauty.domain.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    List<Marca> findByInactivoFalseOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
