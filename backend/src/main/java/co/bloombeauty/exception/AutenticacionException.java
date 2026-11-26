package co.bloombeauty.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 401 credenciales inválidas o 423 cuenta bloqueada (HU-03 E-1, E-2). */
@Getter
public class AutenticacionException extends BloomException {

    private final HttpStatus status;

    private AutenticacionException(HttpStatus status, String codigo, String mensaje) {
        super(codigo, mensaje);
        this.status = status;
    }

    public static AutenticacionException credencialesInvalidas() {
        return new AutenticacionException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                "Correo o contraseña incorrectos");
    }

    public static AutenticacionException cuentaBloqueada(long minutosRestantes) {
        return new AutenticacionException(HttpStatus.LOCKED, "CUENTA_BLOQUEADA",
                "Cuenta bloqueada por intentos fallidos. Intenta de nuevo en " + minutosRestantes + " min");
    }

    public static AutenticacionException cuentaInactiva() {
        return new AutenticacionException(HttpStatus.FORBIDDEN, "CUENTA_INACTIVA",
                "La cuenta está inactiva. Contacta al administrador");
    }
}
