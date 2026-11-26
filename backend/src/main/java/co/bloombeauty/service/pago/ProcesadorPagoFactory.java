package co.bloombeauty.service.pago;

import co.bloombeauty.domain.enums.TipoMetodoPago;
import co.bloombeauty.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN FACTORY. Spring inyecta todas las implementaciones de {@link ProcesadorPago}
 * y la fábrica las indexa por método. El servicio de ventas pide la estrategia sin
 * conocer las clases concretas (Inversión de Dependencias).
 */
@Component
public class ProcesadorPagoFactory {

    private final Map<TipoMetodoPago, ProcesadorPago> procesadores = new EnumMap<>(TipoMetodoPago.class);

    public ProcesadorPagoFactory(List<ProcesadorPago> estrategias) {
        for (ProcesadorPago estrategia : estrategias) {
            estrategia.soporta().forEach(m -> procesadores.put(m, estrategia));
        }
    }

    public ProcesadorPago obtener(TipoMetodoPago metodo) {
        ProcesadorPago p = procesadores.get(metodo);
        if (p == null) {
            throw new ReglaNegocioException("METODO_NO_SOPORTADO", "Método de pago no soportado: " + metodo);
        }
        return p;
    }
}
