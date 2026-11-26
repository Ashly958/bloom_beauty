package co.bloombeauty.service;

/**
 * Puerto de salida para notificaciones. La implementación actual usa SMTP, pero
 * los servicios de negocio no lo saben (DIP): cambiar a SendGrid u otro proveedor
 * solo requiere otra implementación.
 */
public interface NotificacionService {

    void enviarEnlaceRecuperacion(String email, String nombre, String enlace);
}
