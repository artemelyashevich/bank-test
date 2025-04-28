package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret:secret}")
    private String secret;

    public Long extractId(String token) {
        return getClaimsFromToken(token).get("id", Long.class);
    }

    public String generateToken(User user, long tokenLifeTime) {
        return createToken(user, tokenLifeTime);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                    @Override
                    public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
                        return secret.getBytes();
                    }
                })
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createToken(User user, Long tokenLifeTime) {
        var issuedAt = new Date();
        var expirationDate = new Date(issuedAt.getTime() + tokenLifeTime);

        return Jwts.builder()
                .setClaims(Map.of("id", user.getId()))
                .setSubject(user.getEmails().getFirst().getEmail())
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}