package co.bloombeauty.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Respuesta paginada estable (no expone la estructura interna de Spring Data). */
public record PageResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
