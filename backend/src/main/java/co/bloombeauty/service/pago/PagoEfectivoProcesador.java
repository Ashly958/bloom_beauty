package co.bloombeauty.service.pago;

import co.bloombeauty.domain.MetodoPago;
import co.bloombeauty.domain.PagoVenta;
import co.bloombeauty.domain.enums.TipoMetodoPago;
import co.bloombeauty.dto.venta.PagoRequest;
import co.bloombeauty.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/** Estrategia concreta: efectivo. Solo el efectivo genera cambio (HU-01 RN-4, V-3). */
@Component
public class PagoEfectivoProcesador implements ProcesadorPago {

    @Override
    public Set<TipoMetodoPago> soporta() {
        return Set.of(TipoMetodoPago.EFECTIVO);
    }

    @Override
    public PagoVenta procesar(PagoRequest solicitud, MetodoPago metodo) {
        BigDecimal recibido = solicitud.montoRecibido() != null ? solicitud.montoRecibido() : solicitud.monto();
        if (recibido.compareTo(solicitud.monto()) < 0) {
            throw new ReglaNegocioException("EFECTIVO_INSUFICIENTE",
                    "El efectivo recibido es menor al monto a pagar en efectivo");
        }
        PagoVenta pago = new PagoVenta();
        pago.setMetodoPago(metodo);
        pago.setMonto(solicitud.monto());
        pago.setMontoRecibido(recibido);
        pago.setCambio(recibido.subtract(solicitud.monto()));
        return pago;
    }
}
