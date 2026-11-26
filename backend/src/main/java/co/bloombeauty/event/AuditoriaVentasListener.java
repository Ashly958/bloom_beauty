package co.bloombeauty.event;

import co.bloombeauty.domain.Auditoria;
import co.bloombeauty.repository.AuditoriaRepository;
import co.bloombeauty.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Observador que escribe la bitácora de auditoría. Se ejecuta DESPUÉS del commit:
 * si la venta hace rollback no queda un registro de auditoría falso, y un fallo
 * al auditar no revierte una venta ya cobrada.
 */
@Component
@RequiredArgsConstructor
public class AuditoriaVentasListener {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alRegistrarVenta(VentaRegistradaEvent e) {
        guardar(e.usuarioId(), "VENTA_REGISTRADA", e.ventaId(),
                "{\"factura\":\"" + e.numeroFactura() + "\",\"total\":" + e.total().toPlainString() + "}");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alAnularVenta(VentaAnuladaEvent e) {
        guardar(e.adminId(), "VENTA_ANULADA", e.ventaId(),
                "{\"factura\":\"" + e.numeroFactura() + "\",\"motivo\":\"" + e.motivo().replace("\"", "'") + "\"}");
    }

    private void guardar(Long usuarioId, String accion, Long ventaId, String detalleJson) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        a.setAccion(accion);
        a.setEntidad("ventas");
        a.setEntidadId(ventaId);
        a.setDetalle(detalleJson);
        a.setFecha(LocalDateTime.now(clock));
        auditoriaRepository.save(a);
    }
}
