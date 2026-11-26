package co.bloombeauty.service.impl;

import co.bloombeauty.domain.Categoria;
import co.bloombeauty.domain.Marca;
import co.bloombeauty.domain.Producto;
import co.bloombeauty.domain.enums.TipoMovimiento;
import co.bloombeauty.dto.PageResponse;
import co.bloombeauty.dto.producto.ProductoRequest;
import co.bloombeauty.dto.producto.ProductoResponse;
import co.bloombeauty.exception.ConflictoException;
import co.bloombeauty.exception.RecursoNoEncontradoException;
import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.mapper.ProductoMapper;
import co.bloombeauty.repository.CategoriaRepository;
import co.bloombeauty.repository.MarcaRepository;
import co.bloombeauty.repository.ProductoRepository;
import co.bloombeauty.repository.UsuarioRepository;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.InventarioService;
import co.bloombeauty.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private static final int TAMANO_MAXIMO = 50;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;
    private final ProductoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> buscar(String texto, Integer categoriaId, Integer marcaId,
                                                 boolean soloStockBajo, int pagina, int tamano) {
        var pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), TAMANO_MAXIMO),
                Sort.by("nombre"));
        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim();
        return PageResponse.de(productoRepository.buscar(filtro, categoriaId, marcaId, soloStockBajo, pageable)
                .map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return mapper.toResponse(buscarEntidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigo(String codigoBarras) {
        return productoRepository.findByCodigoBarras(codigoBarras).map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", codigoBarras));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest r, UsuarioPrincipal admin) {
        if (productoRepository.existsByCodigoBarras(r.codigoBarras())) {
            throw new ConflictoException("CODIGO_DUPLICADO", "Ya existe un producto con el código " + r.codigoBarras());
        }
        validarPrecios(r);
        Producto p = new Producto();
        mapper.aplicar(r, p, categoria(r.categoriaId()), marca(r.marcaId()));
        productoRepository.save(p);
        // El stock inicial entra como ajuste positivo para que quede en el kárdex
        if (r.stockActual() > 0) {
            inventarioService.registrarMovimiento(p, usuarioRepository.getReferenceById(admin.id()),
                    TipoMovimiento.AJUSTE_POSITIVO, r.stockActual(), "Stock inicial", null, null);
        }
        return mapper.toResponse(p);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest r) {
        Producto p = buscarEntidad(id);
        // Si otro admin editó el producto desde que este lo abrió → 409 (bloqueo optimista)
        if (r.version() != null && !Objects.equals(r.version(), p.getVersion())) {
            throw new ConflictoException("EDICION_CONCURRENTE",
                    "El producto fue modificado por otro usuario. Recarga los datos");
        }
        if (!p.getCodigoBarras().equals(r.codigoBarras()) && productoRepository.existsByCodigoBarras(r.codigoBarras())) {
            throw new ConflictoException("CODIGO_DUPLICADO", "Ya existe un producto con el código " + r.codigoBarras());
        }
        validarPrecios(r);
        mapper.aplicar(r, p, categoria(r.categoriaId()), marca(r.marcaId()));
        return mapper.toResponse(productoRepository.saveAndFlush(p));
    }

    /** Borrado lógico (RF-07, RNF-03): @SQLDelete convierte el DELETE en UPDATE inactivo = true. */
    @Override
    @Transactional
    public void inactivar(Long id) {
        productoRepository.delete(buscarEntidad(id));
    }

    private void validarPrecios(ProductoRequest r) {
        if (r.precioVenta().compareTo(r.precioCosto()) < 0) {
            throw new ReglaNegocioException("PRECIO_INVALIDO", "El precio de venta no puede ser menor al costo");
        }
    }

    private Producto buscarEntidad(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }

    private Categoria categoria(Integer id) {
        return categoriaRepository.findById(id).filter(c -> !c.isInactivo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }

    private Marca marca(Integer id) {
        return marcaRepository.findById(id).filter(m -> !m.isInactivo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca", id));
    }
}
