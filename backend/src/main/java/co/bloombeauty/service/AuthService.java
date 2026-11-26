package co.bloombeauty.service;

import co.bloombeauty.dto.auth.AuthDtos.LoginRequest;
import co.bloombeauty.dto.auth.AuthDtos.LoginResponse;
import co.bloombeauty.dto.auth.AuthDtos.RestablecerRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    /** Siempre responde igual exista o no el correo (HU-03 RN-4). */
    void solicitarRecuperacion(String email);

    void restablecer(RestablecerRequest request);
}
