package co.bloombeauty.service;

import co.bloombeauty.dto.venta.VentaRequest;
import co.bloombeauty.dto.venta.VentaResponse;
import co.bloombeauty.security.UsuarioPrincipal;

/**
 * Contrato del caso de uso de ventas. El controlador depende de esta interfaz y
 * no de la implementación (Inversión de Dependencias / Segregación de Interfaces).
 */
public interface VentaService {

    /** HU-01: registra venta, pagos, descuenta stock y kárdex en una sola transacción. */
    VentaResponse registrar(VentaRequest solicitud, UsuarioPrincipal vendedor);

    VentaResponse obtener(Long id);

    /** HU-12: anula con motivo y reintegra el stock. */
    VentaResponse anular(Long id, String motivo, UsuarioPrincipal admin);
}
