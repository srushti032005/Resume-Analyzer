package com.airesume.analyzer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(jwtSecret)));
    }

    private String toBase64(String secret) {
        // Allow either a raw or base64 secret; pad to a safe length.
        String s = secret;
        while (s.getBytes().length < 64) {
            s = s + s;
        }
        return java.util.Base64.getEncoder().encodeToString(s.getBytes());
    }

    public String generateAccessToken(String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpirationMs))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("type", "REFRESH")
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parse(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        Object role = parse(token).get("role");
        return role == null ? null : role.toString();
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
