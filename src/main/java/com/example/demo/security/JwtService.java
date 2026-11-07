package com.example.demo.security;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Emite y valida JWT firmados con HS256. El secreto y la expiración se
 * configuran por variables de entorno (app.jwt.secret / app.jwt.expiration-ms).
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "app.jwt.secret debe tener al menos 32 bytes para HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    /**
     * Valida firma y expiración. Lanza JwtException si el token es inválido.
     */
    public TokenPayload validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long usuarioId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        Rol rol = Rol.valueOf(claims.get("rol", String.class));
        return new TokenPayload(usuarioId, email, rol);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
