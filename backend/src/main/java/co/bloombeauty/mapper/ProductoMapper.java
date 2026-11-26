package co.bloombeauty.mapper;

import co.bloombeauty.domain.Categoria;
import co.bloombeauty.domain.Marca;
import co.bloombeauty.domain.Producto;
import co.bloombeauty.dto.producto.ProductoRequest;
import co.bloombeauty.dto.producto.ProductoResponse;
import org.springframework.stereotype.Component;

/**
 * PATRÓN DTO + Mapper: las entidades JPA nunca salen del backend. Así se evita
 * exponer columnas internas, serializar proxies LAZY y acoplar el contrato
 * OpenAPI al esquema de la base de datos.
 */
@Component
public class ProductoMapper {

    public ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getCodigoBarras(), p.getNombre(), p.getDescripcion(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getMarca().getId(), p.getMarca().getNombre(),
                p.getTono(), p.getPrecioCosto(), p.getPrecioVenta(), p.getPorcentajeIva(),
                p.getStockActual(), p.getStockMinimo(), p.tieneStockBajo(),
                p.getFechaVencimiento(), p.getImagenUrl(), p.getVersion());
    }

    /** Copia los campos editables del request sobre la entidad (alta o edición). */
    public void aplicar(ProductoRequest r, Producto p, Categoria categoria, Marca marca) {
        p.setCodigoBarras(r.codigoBarras());
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setCategoria(categoria);
        p.setMarca(marca);
        p.setTono(r.tono());
        p.setPrecioCosto(r.precioCosto());
        p.setPrecioVenta(r.precioVenta());
        p.setPorcentajeIva(r.porcentajeIva());
        p.setStockMinimo(r.stockMinimo());
        p.setFechaVencimiento(r.fechaVencimiento());
        p.setImagenUrl(r.imagenUrl());
    }
}
