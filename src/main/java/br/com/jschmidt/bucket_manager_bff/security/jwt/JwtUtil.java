package br.com.jschmidt.bucket_manager_bff.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final int jwtExpirationMs;
    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret,
                   @Value("${jwt.expiration-ms}") int jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
        this.secretKey = jwtSecret;
    }

    public Long getJwtExpirationSeconds() {
        return Duration.ofMillis(jwtExpirationMs).toSeconds();
    }

    public String generateJwtToken(Authentication authentication) {
        String email = "", name = "";

        if (authentication instanceof OAuth2User) {
            OAuth2User userPrincipal = (OAuth2User) authentication.getPrincipal();
            email = userPrincipal.getAttribute("email");
            name = userPrincipal.getAttribute("name");
        }

        if (authentication instanceof Jwt) {
            email = ((Jwt) authentication).getClaimAsString("email");
            name = ((Jwt) authentication).getClaimAsString("name");
        }

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("name", name)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"))
                .compact();
    }

    public String getUserEmailFromJwtToken(String token) {
        Claims claims = parseJwtToken(token);
        return claims.getSubject();
    }

    public Claims parseJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseJwtToken(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
