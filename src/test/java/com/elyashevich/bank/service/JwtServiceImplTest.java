package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.EmailData;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String testSecret = "testSecretKeyWithMinimum32CharactersLength123";
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmails(List.of(new EmailData(1L, testUser, "test@example.com")));
    }

    @Test
    void generateTokenShouldCreateValidJwt() {
        // Given
        long tokenLifetime = TimeUnit.HOURS.toMillis(1);

        // When
        String token = jwtService.generateToken(testUser, tokenLifetime);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertAll(
                () -> assertEquals(testUser.getId(), claims.get("id", Long.class)),
                () -> assertEquals(testUser.getEmails().getFirst().getEmail(), claims.getSubject()),
                () -> assertNotNull(claims.getIssuedAt()),
                () -> assertNotNull(claims.getExpiration()),
                () -> assertTrue(claims.getExpiration().after(new Date()))
        );
    }

    @Test
    void extractIdShouldReturnCorrectUserId() {
        // Given
        long tokenLifetime = TimeUnit.HOURS.toMillis(1);
        String token = jwtService.generateToken(testUser, tokenLifetime);

        // When
        Long extractedId = jwtService.extractId(token);

        // Then
        assertEquals(testUser.getId(), extractedId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1000, 5000, 10000})
    void generateTokenShouldRespectTokenLifetime(long lifetimeMillis) {
        // When
        String token = jwtService.generateToken(testUser, lifetimeMillis);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Then
        long actualLifetime = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(lifetimeMillis, actualLifetime, 1000); // Allow 1 second variance
    }

    @Test
    void extractIdShouldThrowForExpiredToken() throws InterruptedException {
        // Given
        long shortLifetime = 100; // 100ms
        String token = jwtService.generateToken(testUser, shortLifetime);
        Thread.sleep(200); // Wait for token to expire

        // When & Then
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, 
                () -> jwtService.extractId(token));
    }

    @Test
    void extractIdShouldThrowForMalformedToken() {
        // Given
        String malformedToken = "header.payload.signature";

        // When & Then
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, 
                () -> jwtService.extractId(malformedToken));
    }

    @Test
    void tokensShouldBeDifferentForDifferentUsers() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmails(List.of(new EmailData(2L, anotherUser, "another@example.com")));
        long tokenLifetime = 3600000;

        // When
        String token1 = jwtService.generateToken(testUser, tokenLifetime);
        String token2 = jwtService.generateToken(anotherUser, tokenLifetime);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void tokensShouldContainCorrectClaims() {
        // Given
        long tokenLifetime = 3600000;

        // When
        String token = jwtService.generateToken(testUser, tokenLifetime);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Then
        assertAll(
                () -> assertEquals(testUser.getId(), claims.get("id", Long.class)),
                () -> assertEquals(testUser.getEmails().getFirst().getEmail(), claims.getSubject()),
                () -> assertNotNull(claims.getIssuedAt()),
                () -> assertNotNull(claims.getExpiration())
        );
    }

    @Test
    void shouldUseDifferentSecretKey() {
        // Given
        String newSecret = "newSecretKeyWithMinimum32CharactersLength456";
        ReflectionTestUtils.setField(jwtService, "secret", newSecret);
        long tokenLifetime = 3600000;
        String token = jwtService.generateToken(testUser, tokenLifetime);

        // When & Then
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
        });
    }
}