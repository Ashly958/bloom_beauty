package co.bloombeauty.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite y valida tokens JWT HS256 con vigencia de 8 h (RNF-06). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration vigencia;
    private final Clock clock;

    public JwtService(@Value("${bloom.jwt.secret}") String secreto,
                      @Value("${bloom.jwt.vigencia:PT8H}") Duration vigencia,
                      Clock clock) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.vigencia = vigencia;
        this.clock = clock;
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    public TokenEmitido emitir(UsuarioPrincipal usuario) {
        Instant ahora = clock.instant();
        Instant expira = ahora.plus(vigencia);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim("email", usuario.email())
                .claim("rol", usuario.rol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new TokenEmitido(token, expira);
    }

    public Optional<UsuarioPrincipal> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioPrincipal(Long.valueOf(c.getSubject()),
                    c.get("email", String.class), c.get("rol", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
