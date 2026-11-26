package co.bloombeauty.service;

import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.producto.ProductoRequest;
import co.bloombeauty.dto.producto.ProductoResponse;
import co.bloombeauty.security.UsuarioPrincipal;

public interface ProductoService {

    PageResponse<ProductoResponse> buscar(String texto, Integer categoriaId, Integer marcaId,
                                          boolean soloStockBajo, int pagina, int tamano);

    ProductoResponse obtener(Long id);

    ProductoResponse obtenerPorCodigo(String codigoBarras);

    ProductoResponse crear(ProductoRequest request, UsuarioPrincipal admin);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void inactivar(Long id);
}
