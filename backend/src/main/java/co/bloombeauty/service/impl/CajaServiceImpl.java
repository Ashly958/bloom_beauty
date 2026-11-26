package co.bloombeauty.service.impl;

import co.bloombeauty.domain.ConfiguracionNegocio;
import co.bloombeauty.domain.SesionCaja;
import co.bloombeauty.domain.enums.EstadoCaja;
import co.bloombeauty.dto.caja.CajaDtos.AbrirCajaRequest;
import co.bloombeauty.dto.caja.CajaDtos.CajaResponse;
import co.bloombeauty.dto.caja.CajaDtos.CerrarCajaRequest;
import co.bloombeauty.exception.CajaNoDisponibleException;
import co.bloombeauty.repository.ConfiguracionNegocioRepository;
import co.bloombeauty.repository.SesionCajaRepository;
import co.bloombeauty.repository.UsuarioRepository;
import co.bloombeauty.security.UsuarioPrincipal;
import co.bloombeauty.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Apertura (RF-20) y arqueo/cierre de caja (RF-21, HU-02). */
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final SesionCajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionNegocioRepository configuracionRepository;
    private final Clock clock;

    @Override
    @Transactional
    public CajaResponse abrir(AbrirCajaRequest request, UsuarioPrincipal usuario) {
        // Bloquea la fila de configuración: serializa aperturas y garantiza una sola caja ABIERTA
        configuracionRepository.bloquearParaConsecutivo();
        if (cajaRepository.existsByEstado(EstadoCaja.ABIERTA)) {
            throw new CajaNoDisponibleException("Ya existe una caja abierta");
        }
        SesionCaja caja = SesionCaja.abrir(usuarioRepository.getReferenceById(usuario.id()),
                request.montoInicial(), LocalDateTime.now(clock));
        return toResponse(cajaRepository.save(caja));
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse actual() {
        return toResponse(cajaAbierta());
    }

    @Override
    @Transactional
    public CajaResponse cerrar(CerrarCajaRequest r, UsuarioPrincipal admin) {
        SesionCaja caja = cajaAbierta();
        ConfiguracionNegocio cfg = configuracionRepository.obtener()
                .orElseThrow(() -> new IllegalStateException("Falta la configuración del negocio"));
        caja.cerrar(usuarioRepository.getReferenceById(admin.id()), r.efectivoDeclarado(),
                r.transferenciaDeclarada(), r.tarjetaDeclarada(), r.observaciones(),
                cfg.getUmbralDiferenciaCaja(), LocalDateTime.now(clock));
        // @Version en SesionCaja: si otro admin cerró al mismo tiempo → 409 "La caja ya fue cerrada"
        return toResponse(cajaRepository.saveAndFlush(caja));
    }

    private SesionCaja cajaAbierta() {
        return cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaNoDisponibleException("No existe una caja abierta"));
    }

    private CajaResponse toResponse(SesionCaja c) {
        return new CajaResponse(c.getId(), c.getEstado(), c.getFechaApertura(), c.getFechaCierre(),
                c.getUsuarioApertura().getNombreCompleto(), c.getMontoInicial(),
                c.getTotalVentasEfectivo(), c.getTotalVentasTransferencia(), c.getTotalVentasTarjeta(),
                c.getTotalGastosEfectivo(), c.calcularEfectivoEsperado(), c.getDiferencia(), c.getObservaciones());
    }
}
