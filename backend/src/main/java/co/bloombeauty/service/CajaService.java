package co.bloombeauty.service;

import co.bloombeauty.dto.caja.CajaDtos.AbrirCajaRequest;
import co.bloombeauty.dto.caja.CajaDtos.CajaResponse;
import co.bloombeauty.dto.caja.CajaDtos.CerrarCajaRequest;
import co.bloombeauty.security.UsuarioPrincipal;

public interface CajaService {

    CajaResponse abrir(AbrirCajaRequest request, UsuarioPrincipal usuario);

    CajaResponse actual();

    CajaResponse cerrar(CerrarCajaRequest request, UsuarioPrincipal admin);
}
