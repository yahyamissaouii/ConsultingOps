package com.consultingops.timesheetservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.security.jwt-secret}") String jwtSecret) {
        byte[] keyBytes = jwtSecret.matches("^[A-Za-z0-9+/=]+$")
                ? safeDecode(jwtSecret)
                : jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID consultantId = claims.get("consultantId") == null
                ? null
                : UUID.fromString(claims.get("consultantId", String.class));

        return new UserPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                Enum.valueOf(com.consultingops.timesheetservice.entity.enums.UserRole.class, claims.get("role", String.class)),
                consultantId
        );
    }

    private byte[] safeDecode(String value) {
        try {
            return Decoders.BASE64.decode(value);
        } catch (IllegalArgumentException exception) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }
}
