package com.resourcebooking.server.controller;

import com.resourcebooking.server.dto.request.LoginRequest;
import com.resourcebooking.server.dto.request.RegisterRequest;
import com.resourcebooking.server.dto.response.LoginResponse;
import com.resourcebooking.server.dto.response.UserResponse;
import com.resourcebooking.server.security.AuthCookieHelper;
import com.resourcebooking.server.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieHelper authCookieHelper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.LoginResult loginResult = authService.login(request);

        authCookieHelper.addAuthCookies(
                response,
                loginResult.accessToken(),
                loginResult.refreshToken()
        );

        return loginResult.response();
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(
            @CookieValue(name = "${app.security.cookie.refresh-token-name}", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthService.TokenRefreshResult refreshResult = authService.refresh(refreshToken);

        authCookieHelper.addAuthCookies(
                response,
                refreshResult.accessToken(),
                refreshResult.refreshToken()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = "${app.security.cookie.refresh-token-name}", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        authCookieHelper.clearAuthCookies(response);
    }
}