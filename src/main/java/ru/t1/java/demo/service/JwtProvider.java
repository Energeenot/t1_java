package ru.t1.java.demo.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@PropertySource("classpath:jwt.properties")
public class JwtProvider {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtProvider(@Value("${jwt.secret}")String secret, @Value("${jwt.expiration}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
    }

    public String generateTokenForService() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claim("sub", "Service1")
                .claim("iat", now)
                .claim("exp", expiryDate)
                .signWith(secretKey)
                .compact();
    }

}
