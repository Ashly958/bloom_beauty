package co.bloombeauty.service.impl;

import co.bloombeauty.domain.*;
import co.bloombeauty.domain.enums.EstadoCaja;
import co.bloombeauty.domain.enums.EstadoUsuario;
import co.bloombeauty.domain.enums.TipoMovimiento;
import co.bloombeauty.dto.venta.LineaVentaRequest;
import co.bloombeauty.dto.venta.PagoRequest;
import co.bloombeauty.dto.venta.VentaRequest;
import co.bloombeauty.dto.venta.VentaResponse;
import co.bloombeauty.event.VentaAnuladaEvent;
import co.bloombeauty.event.VentaRegistradaEvent;
import co.bloombeauty.exception.CajaNoDisponibleException;
import co.bloombeauty.exception.RecursoNoEncontradoException;
import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.mapper.VentaMapper;
import co.bloombeauty.repository.*;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.InventarioService;
import co.bloombeauty.service.NumeradorFacturas;
import co.bloombeauty.service.PoliticaDescuento;
import co.bloombeauty.service.VentaService;
import co.bloombeauty.service.pago.ProcesadorPagoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso crítico HU-01 (Registrar venta y pago en el POS).
 *
 * <p>SOLID aplicado:
 * <ul>
 *   <li>S: orquesta el caso de uso; las reglas viven en el dominio (Venta, Producto, SesionCaja),
 *       en {@link PoliticaDescuento}, en las estrategias de pago y en {@link InventarioService}.</li>
 *   <li>O: un método de pago nuevo se agrega con otra estrategia, sin tocar esta clase.</li>
 *   <li>D: depende de abstracciones (repositorios, InventarioService, ProcesadorPagoFactory)
 *       inyectadas por constructor (@RequiredArgsConstructor sobre campos final).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final ConfiguracionNegocioRepository configuracionRepository;
    private final InventarioService inventarioService;
    private final ProcesadorPagoFactory procesadorPagoFactory;
    private final PoliticaDescuento politicaDescuento;
    private final NumeradorFacturas numeradorFacturas;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;
    private final VentaMapper ventaMapper;
    private final Clock clock;

    @Override
    @Transactional
    public VentaResponse registrar(VentaRequest solicitud, UsuarioPrincipal vendedor) {
        // 1. Debe existir una caja ABIERTA (RF-20, HU-01 E-4) → 409
        SesionCaja caja = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaNoDisponibleException("No hay una caja abierta. Abre la caja para vender"));

        ConfiguracionNegocio config = configuracionRepository.obtener()
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del negocio"));
        boolean autorizadoPorAdmin = validarClaveAdministrador(solicitud.claveAutorizacion());

        // 2. Cargar productos de todas las líneas en una sola consulta
        Map<Long, Producto> productos = cargarProductos(solicitud.lineas());

        Venta venta = new Venta();
        venta.setUsuario(usuarioRepository.getReferenceById(vendedor.id()));
        venta.setSesionCaja(caja);
        venta.setFecha(LocalDateTime.now(clock));
        if (solicitud.clienteId() != null) {
            venta.setCliente(clienteRepository.findById(solicitud.clienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", solicitud.clienteId())));
        }

        // 3. Líneas: política de descuento + cálculo de subtotal/IVA en el dominio
        for (LineaVentaRequest linea : solicitud.lineas()) {
            politicaDescuento.validar(linea.porcentajeDescuento(), vendedor, autorizadoPorAdmin, config);
            Producto p = productos.get(linea.productoId());
            venta.agregarDetalle(DetalleVenta.de(p, linea.cantidad(), linea.porcentajeDescuento()));
        }
        venta.recalcularTotales();

        // 4. Pagos: cada método se procesa con su estrategia (Strategy + Factory)
        for (PagoRequest pr : solicitud.pagos()) {
            MetodoPago metodo = metodoPagoRepository.findByNombreAndActivoTrue(pr.metodo())
                    .orElseThrow(() -> new ReglaNegocioException("METODO_NO_DISPONIBLE",
                            "Método de pago no disponible: " + pr.metodo()));
            venta.agregarPago(procesadorPagoFactory.obtener(pr.metodo()).procesar(pr, metodo));
            caja.registrarIngreso(pr.metodo(), pr.monto());
        }
        venta.validarPagosCubrenTotal(); // 422 PAGO_INCOMPLETO / PAGO_EXCEDIDO

        // 5. Consecutivo BB-XXXXXX con bloqueo pesimista sobre configuracion_negocio
        venta.setNumeroFactura(numeradorFacturas.siguiente());
        ventaRepository.save(venta);

        // 6. Descuento de stock + kárdex. Producto.descontarStock lanza 409 si no alcanza;
        //    @Version lanza 409 si otro trabajador vendió la misma unidad (RNF-02).
        for (DetalleVenta d : venta.getDetalles()) {
            inventarioService.registrarMovimiento(d.getProducto(), venta.getUsuario(),
                    TipoMovimiento.SALIDA_VENTA, d.getCantidad(), "Venta " + venta.getNumeroFactura(), venta, null);
        }
        productoRepository.flush(); // fuerza el UPDATE ... WHERE version = ? dentro del método

        // 7. Notificar observadores (auditoría, alertas de stock) tras el commit
        eventos.publishEvent(new VentaRegistradaEvent(venta.getId(), venta.getNumeroFactura(), vendedor.id(),
                venta.getTotal(), List.copyOf(productos.keySet())));

        return ventaMapper.toResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return ventaMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public VentaResponse anular(Long id, String motivo, UsuarioPrincipal admin) {
        Venta venta = buscar(id);
        Usuario usuarioAdmin = usuarioRepository.getReferenceById(admin.id());
        venta.anular(motivo, usuarioAdmin, LocalDateTime.now(clock)); // 409 si ya anulada o caja cerrada

        for (DetalleVenta d : venta.getDetalles()) {
            inventarioService.registrarMovimiento(d.getProducto(), usuarioAdmin, TipoMovimiento.REINTEGRO_ANULACION,
                    d.getCantidad(), "Anulación " + venta.getNumeroFactura() + ": " + motivo, venta, null);
        }
        venta.getPagos().forEach(p -> venta.getSesionCaja().revertirIngreso(p.getMetodoPago().getNombre(), p.getMonto()));

        eventos.publishEvent(new VentaAnuladaEvent(venta.getId(), venta.getNumeroFactura(), admin.id(),
                venta.getTotal(), motivo));
        return ventaMapper.toResponse(venta);
    }

    // ---------- Métodos privados de apoyo ----------

    private Venta buscar(Long id) {
        return ventaRepository.findWithDetallesById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));
    }

    private Map<Long, Producto> cargarProductos(List<LineaVentaRequest> lineas) {
        List<Long> ids = lineas.stream().map(LineaVentaRequest::productoId).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ReglaNegocioException("PRODUCTO_DUPLICADO",
                    "Un producto aparece en varias líneas; ajusta la cantidad en una sola línea");
        }
        Map<Long, Producto> productos = productoRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        ids.stream().filter(id -> !productos.containsKey(id)).findFirst().ifPresent(id -> {
            throw new ReglaNegocioException("PRODUCTO_NO_DISPONIBLE", "Producto no disponible: " + id);
        });
        return productos;
    }

    private boolean validarClaveAdministrador(String clave) {
        if (clave == null || clave.isBlank()) {
            return false;
        }
        boolean valida = usuarioRepository.findByRol_NombreAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO).stream()
                .anyMatch(admin -> passwordEncoder.matches(clave, admin.getPasswordHash()));
        if (!valida) {
            throw new ReglaNegocioException("AUTORIZACION_INVALIDA", "La clave de autorización no es válida");
        }
        return true;
    }
}
