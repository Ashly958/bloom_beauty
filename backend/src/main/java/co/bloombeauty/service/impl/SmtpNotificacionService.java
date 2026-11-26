package co.bloombeauty.service.impl;

import co.bloombeauty.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpNotificacionService implements NotificacionService {

    private final JavaMailSender mailSender;

    @Value("${bloom.mail.remitente:no-responder@bloombeauty.co}")
    private String remitente;

    @Async
    @Override
    public void enviarEnlaceRecuperacion(String email, String nombre, String enlace) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(remitente);
        msg.setTo(email);
        msg.setSubject("Bloom Beauty - Restablecer contraseña");
        msg.setText("Hola " + nombre + ",\n\nPara restablecer tu contraseña abre este enlace (vence en 30 minutos):\n"
                + enlace + "\n\nSi no lo solicitaste, ignora este mensaje.");
        try {
            mailSender.send(msg);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo de recuperación a {}", email, e);
        }
    }
}
