package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.LoginRequest;
import com.resourcebooking.server.dto.request.RegisterRequest;
import com.resourcebooking.server.dto.response.LoginResponse;
import com.resourcebooking.server.dto.response.UserResponse;
import com.resourcebooking.server.entity.RefreshToken;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.exception.EmailAlreadyExistsException;
import com.resourcebooking.server.mapper.UserMapper;
import com.resourcebooking.server.repository.UserRepository;
import com.resourcebooking.server.security.JwtTokenProvider;
import com.resourcebooking.server.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        log.info("User registered userId={} role={}", savedUser.getId(), savedUser.getRole());

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        RefreshToken refreshToken = refreshTokenService.createForUser(user);

        LoginResponse response = new LoginResponse(
                principal.getRole(),
                principal.getName()
        );

        log.info("Login succeeded userId={} role={}", principal.getId(), principal.getRole());

        return new LoginResult(
                response,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Transactional
    public TokenRefreshResult refresh(String refreshTokenValue) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(refreshTokenValue);
        UserPrincipal principal = UserPrincipal.from(newRefreshToken.getUser());

        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        log.info("Access token refreshed userId={}", principal.getId());

        return new TokenRefreshResult(
                accessToken,
                newRefreshToken.getToken()
        );
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenService.deleteByToken(refreshTokenValue);
        }
    }

    public record LoginResult(
            LoginResponse response,
            String accessToken,
            String refreshToken
    ) {
    }

    public record TokenRefreshResult(
            String accessToken,
            String refreshToken
    ) {
    }
}