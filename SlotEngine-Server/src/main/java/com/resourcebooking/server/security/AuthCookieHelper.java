package com.resourcebooking.server.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieHelper {

    @Value("${app.security.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Value("${app.security.cookie.refresh-token-name}")
    private String refreshTokenCookieName;

    @Value("${app.security.cookie.access-token-path}")
    private String accessTokenPath;

    @Value("${app.security.cookie.refresh-token-path}")
    private String refreshTokenPath;

    @Value("${app.security.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${app.security.refresh-token.expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${app.security.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.security.cookie.same-site}")
    private String sameSite;

    public void addAuthCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        addCookie(
                response,
                accessTokenCookieName,
                accessToken,
                accessTokenPath,
                Duration.ofMinutes(accessTokenExpirationMinutes)
        );

        addCookie(
                response,
                refreshTokenCookieName,
                refreshToken,
                refreshTokenPath,
                Duration.ofDays(refreshTokenExpirationDays)
        );
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(
                response,
                accessTokenCookieName,
                "",
                accessTokenPath,
                Duration.ZERO
        );

        addCookie(
                response,
                refreshTokenCookieName,
                "",
                refreshTokenPath,
                Duration.ZERO
        );
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            String path,
            Duration maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}