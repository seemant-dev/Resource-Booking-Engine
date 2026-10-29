package com.resourcebooking.server.security;

import com.resourcebooking.server.config.properties.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthCookieHelper {

    private final CookieProperties cookieProperties;

    @Value("${app.security.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${app.security.refresh-token.expiration-days}")
    private long refreshTokenExpirationDays;

    public void addAuthCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        addCookie(
                response,
                cookieProperties.getAccessTokenName(),
                accessToken,
                cookieProperties.getAccessTokenPath(),
                Duration.ofMinutes(accessTokenExpirationMinutes)
        );

        addCookie(
                response,
                cookieProperties.getRefreshTokenName(),
                refreshToken,
                cookieProperties.getRefreshTokenPath(),
                Duration.ofDays(refreshTokenExpirationDays)
        );
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(
                response,
                cookieProperties.getAccessTokenName(),
                "",
                cookieProperties.getAccessTokenPath(),
                Duration.ZERO
        );

        addCookie(
                response,
                cookieProperties.getRefreshTokenName(),
                "",
                cookieProperties.getRefreshTokenPath(),
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
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(path)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
