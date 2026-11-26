package co.bloombeauty.dto.producto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoResponse(
        Long id,
        String codigoBarras,
        String nombre,
        String descripcion,
        Integer categoriaId,
        String categoria,
        Integer marcaId,
        String marca,
        String tono,
        BigDecimal precioCosto,
        BigDecimal precioVenta,
        BigDecimal porcentajeIva,
        int stockActual,
        int stockMinimo,
        boolean stockBajo,
        LocalDate fechaVencimiento,
        String imagenUrl,
        Integer version
) {
}
