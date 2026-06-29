package com.taskflow.gestaocolaboradores.shared.security;

import com.taskflow.gestaocolaboradores.shared.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserPrincipal principal) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("name", principal.getFullName())
                .claim("email", principal.getUsername())
                .claim("role", principal.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) props.expirationMinutes() * 60_000))
                .signWith(key())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UserPrincipal principalFromToken(String token) {
        Claims claims = parseToken(token);
        return new UserPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                null,
                claims.get("name", String.class),
                Role.valueOf(claims.get("role", String.class)));
    }
}