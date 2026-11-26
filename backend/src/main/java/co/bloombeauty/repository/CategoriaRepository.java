package co.bloombeauty.repository;

import co.bloombeauty.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findByInactivoFalseOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
