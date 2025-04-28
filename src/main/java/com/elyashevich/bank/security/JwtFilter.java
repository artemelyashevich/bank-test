package com.elyashevich.bank.security;

import com.elyashevich.bank.exception.InvalidTokenException;
import com.elyashevich.bank.service.JwtService;
import com.elyashevich.bank.util.HandleSecurityErrorUtil;
import com.elyashevich.bank.util.SafetyExtractIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_ERROR_MESSAGE = "Authentication error: {}";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            var jwt = authHeader.substring(TOKEN_PREFIX.length());
            var id = extractId(jwt);

            if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                var context = SecurityContextHolder.createEmptyContext();
                var authToken = new UsernamePasswordAuthenticationToken(
                        id,
                        null,
                        null
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.warn(AUTH_ERROR_MESSAGE, e.getMessage());
            handleAuthenticationError(response, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected authentication error", e);
            handleAuthenticationError(response, "Internal authentication error");
        }
    }

    private Long extractId(String token) {
        return SafetyExtractIdUtil.extractEmailClaims(() -> jwtService.extractId(token));
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        HandleSecurityErrorUtil.handleError(response, message).getWriter().flush();
    }
}
