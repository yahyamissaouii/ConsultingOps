package com.consultingops.userservice.security;

import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public String generateToken(User user, Consultant consultant) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        if (consultant != null) {
            claims.put("consultantId", consultant.getId().toString());
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(8, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
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
                Enum.valueOf(com.consultingops.userservice.entity.enums.UserRole.class, claims.get("role", String.class)),
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
