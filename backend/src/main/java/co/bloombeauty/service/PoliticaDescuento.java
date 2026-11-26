package co.bloombeauty.service;

import co.bloombeauty.domain.ConfiguracionNegocio;
import co.bloombeauty.exception.ReglaNegocioException;
import co.bloombeauty.security.UsuarioPrincipal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Regla de descuento por línea (HU-01 RN-3): el Trabajador aplica hasta el máximo
 * configurado (10 %); por encima y hasta 30 % requiere autorización del Administrador.
 * Responsabilidad única: el servicio de ventas delega aquí la decisión.
 */
@Component
public class PoliticaDescuento {

    static final BigDecimal MAXIMO_ABSOLUTO = new BigDecimal("30.00");

    public void validar(BigDecimal porcentaje, UsuarioPrincipal vendedor, boolean autorizadoPorAdmin,
                        ConfiguracionNegocio config) {
        if (porcentaje == null || porcentaje.signum() == 0) {
            return;
        }
        if (porcentaje.compareTo(MAXIMO_ABSOLUTO) > 0) {
            throw new ReglaNegocioException("DESCUENTO_EXCEDIDO", "El descuento máximo permitido es 30 %");
        }
        boolean puedeSinAutorizacion = vendedor.esAdministrador()
                || porcentaje.compareTo(config.getDescuentoMaximoTrabajador()) <= 0;
        if (!puedeSinAutorizacion && !autorizadoPorAdmin) {
            throw new ReglaNegocioException("DESCUENTO_REQUIERE_AUTORIZACION",
                    "Descuentos mayores a " + config.getDescuentoMaximoTrabajador().toPlainString()
                            + " % requieren clave del administrador");
        }
    }
}
