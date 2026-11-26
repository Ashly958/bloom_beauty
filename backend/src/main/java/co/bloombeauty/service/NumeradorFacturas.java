package co.bloombeauty.service;

import co.bloombeauty.domain.ConfiguracionNegocio;
import co.bloombeauty.repository.ConfiguracionNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera el consecutivo BB-000001 (RF-18). Bloquea la fila de configuración con
 * SELECT ... FOR UPDATE dentro de la transacción de la venta, de modo que dos
 * ventas simultáneas nunca obtienen el mismo número y, si la venta hace rollback,
 * el consecutivo tampoco se consume.
 */
@Component
@RequiredArgsConstructor
public class NumeradorFacturas {

    private final ConfiguracionNegocioRepository configuracionRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente() {
        ConfiguracionNegocio cfg = configuracionRepository.bloquearParaConsecutivo()
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del negocio"));
        int numero = cfg.siguienteConsecutivo();
        return String.format("%s-%06d", cfg.getPrefijoFactura(), numero);
    }
}
