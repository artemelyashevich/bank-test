package com.elyashevich.bank.util;

import com.elyashevich.bank.exception.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@UtilityClass
public class SafetyExtractIdUtil {

    public static Long extractEmailClaims(Supplier<Long> action) {
        try {
            return action.get();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("JWT expired at " + e.getClaims().getExpiration() +
                    ". Current time: " + LocalDateTime.now());
        } catch (SignatureException e) {
            throw new InvalidTokenException("JWT signature does not match locally computed signature. " +
                    "JWT validity cannot be asserted and should not be trusted.");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("Malformed JWT: " + e.getMessage() +
                    ". JWT must consist of three base64-encoded parts separated by dots");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("Unsupported JWT: " + e.getMessage() +
                    ". Expected format: header.payload.signature");
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("JWT claims string is empty or invalid: " + e.getMessage());
        }
    }
}