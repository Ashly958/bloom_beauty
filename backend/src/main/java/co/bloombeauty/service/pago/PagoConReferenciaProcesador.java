package co.bloombeauty.service.pago;

import co.bloombeauty.domain.MetodoPago;
import co.bloombeauty.domain.PagoVenta;
import co.bloombeauty.domain.enums.TipoMetodoPago;
import co.bloombeauty.dto.venta.PagoRequest;
import co.bloombeauty.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/** Estrategia concreta: transferencia y tarjetas. Exigen referencia de 4–40 caracteres (V-4). */
@Component
public class PagoConReferenciaProcesador implements ProcesadorPago {

    @Override
    public Set<TipoMetodoPago> soporta() {
        return Set.of(TipoMetodoPago.TRANSFERENCIA, TipoMetodoPago.TARJETA_DEBITO, TipoMetodoPago.TARJETA_CREDITO);
    }

    @Override
    public PagoVenta procesar(PagoRequest solicitud, MetodoPago metodo) {
        String ref = solicitud.referencia();
        if (ref == null || ref.isBlank() || ref.length() < 4 || ref.length() > 40) {
            throw new ReglaNegocioException("REFERENCIA_REQUERIDA",
                    "La referencia es obligatoria para " + solicitud.metodo() + " (4 a 40 caracteres)");
        }
        PagoVenta pago = new PagoVenta();
        pago.setMetodoPago(metodo);
        pago.setMonto(solicitud.monto());
        pago.setReferencia(ref.trim());
        pago.setCambio(BigDecimal.ZERO);
        return pago;
    }
}
