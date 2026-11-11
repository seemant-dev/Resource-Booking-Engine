package com.resourcebooking.server.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.server.entity.RefreshToken;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.integration.AbstractPostgresIntegrationTest;
import com.resourcebooking.server.repository.RefreshTokenRepository;
import com.resourcebooking.server.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerShouldCreateUser() throws Exception {
        String email = uniqueEmail("register");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Aisha Khan",
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Aisha Khan"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void loginShouldReturnUserSummaryAndSetAuthCookies() throws Exception {
        String email = uniqueEmail("login");
        registerUser("Aisha Khan", email, PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.name").value("Aisha Khan"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void logoutShouldRevokeRefreshTokenAndRejectReuse() throws Exception {
        String email = uniqueEmail("logout");
        registerUser("Logout User", email, PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie refreshToken = loginResult.getResponse().getCookie("refresh_token");

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getPath()).isEqualTo("/api/auth");
        assertThat(refreshTokenRepository.findByToken(refreshToken.getValue())).isPresent();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));

        assertThat(refreshTokenRepository.findByToken(refreshToken.getValue())).isEmpty();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void loginShouldPreserveRefreshTokensForOtherSessions() throws Exception {
        String email = uniqueEmail("multi-session");
        registerUser("Multi Session User", email, PASSWORD);

        Cookie firstSessionRefreshToken = loginAndGetRefreshToken(email, PASSWORD);
        Cookie secondSessionRefreshToken = loginAndGetRefreshToken(email, PASSWORD);

        RefreshToken firstStoredToken = refreshTokenRepository
                .findByToken(firstSessionRefreshToken.getValue())
                .orElseThrow();
        RefreshToken secondStoredToken = refreshTokenRepository
                .findByToken(secondSessionRefreshToken.getValue())
                .orElseThrow();

        assertThat(firstStoredToken.getUser().getId())
                .isEqualTo(secondStoredToken.getUser().getId());
        assertThat(firstStoredToken.getToken())
                .isNotEqualTo(secondStoredToken.getToken());
    }

    @Test
    void concurrentRefreshShouldAllowExactlyOneRotation() throws Exception {
        String email = uniqueEmail("concurrent-refresh");
        registerUser("Concurrent Refresh User", email, PASSWORD);

        Cookie refreshToken = loginAndGetRefreshToken(email, PASSWORD);
        Cookie otherSessionRefreshToken = loginAndGetRefreshToken(email, PASSWORD);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<MvcResult> firstAttempt = executorService.submit(
                    () -> refreshConcurrently(refreshToken.getValue(), readyLatch, startLatch)
            );
            Future<MvcResult> secondAttempt = executorService.submit(
                    () -> refreshConcurrently(refreshToken.getValue(), readyLatch, startLatch)
            );

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            List<MvcResult> results = List.of(
                    firstAttempt.get(10, TimeUnit.SECONDS),
                    secondAttempt.get(10, TimeUnit.SECONDS)
            );

            assertThat(results)
                    .extracting(result -> result.getResponse().getStatus())
                    .containsExactlyInAnyOrder(204, 401);

            MvcResult successfulResult = results.stream()
                    .filter(result -> result.getResponse().getStatus() == 204)
                    .findFirst()
                    .orElseThrow();
            MvcResult rejectedResult = results.stream()
                    .filter(result -> result.getResponse().getStatus() == 401)
                    .findFirst()
                    .orElseThrow();

            Cookie replacementRefreshToken = successfulResult.getResponse().getCookie("refresh_token");

            assertThat(replacementRefreshToken).isNotNull();
            assertThat(replacementRefreshToken.getValue()).isNotEqualTo(refreshToken.getValue());
            assertThat(objectMapper.readTree(rejectedResult.getResponse().getContentAsString())
                    .path("error")
                    .asText()).isEqualTo("REFRESH_TOKEN_INVALID");

            assertThat(refreshTokenRepository.findByToken(refreshToken.getValue())).isEmpty();
            assertThat(refreshTokenRepository.findByToken(replacementRefreshToken.getValue())).isPresent();
            assertThat(refreshTokenRepository.findByToken(otherSessionRefreshToken.getValue())).isPresent();
        } finally {
            startLatch.countDown();
            executorService.shutdownNow();
        }
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {
        String email = uniqueEmail("duplicate");
        registerUser("First User", email, PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Second User",
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void loginShouldRejectInvalidCredentials() throws Exception {
        String email = uniqueEmail("invalid-login");
        registerUser("Invalid Login User", email, PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", "WrongPassword@123"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void protectedRouteShouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/bookings/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void userShouldBeBlockedFromAdminResourceCreation() throws Exception {
        String email = uniqueEmail("user-admin-blocked");
        registerUser("Regular User", email, PASSWORD);
        Cookie accessToken = loginAndGetAccessToken(email, PASSWORD);

        mockMvc.perform(post("/api/resources")
                        .cookie(accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "User Blocked Resource " + System.nanoTime(),
                                "description", "Should not be created by USER"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void adminShouldBeAllowedToCreateResource() throws Exception {
        String email = createAdminUser();
        Cookie accessToken = loginAndGetAccessToken(email, PASSWORD);
        String resourceName = "Admin Resource " + System.nanoTime();

        mockMvc.perform(post("/api/resources")
                        .cookie(accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", resourceName,
                                "description", "Created by ADMIN"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value(resourceName))
                .andExpect(jsonPath("$.description").value("Created by ADMIN"));
    }

    @Test
    void refreshShouldRotateTokenAndPersistOnlyTheReplacement() throws Exception {
        String email = uniqueEmail("rotation");
        registerUser("Rotation User", email, PASSWORD);

        Cookie originalToken = loginAndGetRefreshToken(email, PASSWORD);
        Long userId = refreshTokenRepository.findByToken(originalToken.getValue())
                .orElseThrow()
                .getUser()
                .getId();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(originalToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie replacementToken = refreshResult.getResponse().getCookie("refresh_token");

        assertThat(replacementToken).isNotNull();
        assertThat(replacementToken.getValue()).isNotEqualTo(originalToken.getValue());
        assertThat(refreshTokenRepository.findByToken(originalToken.getValue())).isEmpty();
        assertThat(refreshTokenRepository.findByToken(replacementToken.getValue()))
                .get()
                .extracting(token -> token.getUser().getId())
                .isEqualTo(userId);
    }

    @Test
    void refreshShouldRejectReuseOfRotatedToken() throws Exception {
        String email = uniqueEmail("rotated-token-reuse");
        registerUser("Rotated Token User", email, PASSWORD);
        Cookie originalToken = loginAndGetRefreshToken(email, PASSWORD);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(originalToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(originalToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void refreshShouldRejectAndRemoveExpiredToken() throws Exception {
        String email = uniqueEmail("expired-refresh-token");
        registerUser("Expired Token User", email, PASSWORD);
        User user = userRepository.findByEmail(email).orElseThrow();

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(user);
        expiredToken.setToken("expired-token-" + System.nanoTime());
        expiredToken.setExpiresAt(Instant.now().minusSeconds(60));
        refreshTokenRepository.saveAndFlush(expiredToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", expiredToken.getToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("REFRESH_TOKEN_INVALID"));

        assertThat(refreshTokenRepository.findByToken(expiredToken.getToken())).isEmpty();
    }

    @Test
    void refreshShouldRejectUnknownToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "not-a-stored-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void refreshShouldRejectMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void authLifecycleShouldPreserveCookieSecurityAttributes() throws Exception {
        String email = uniqueEmail("cookie-contract");
        registerUser("Cookie Contract User", email, PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        assertAuthCookies(loginResult, false);

        Cookie loginRefreshToken = loginResult.getResponse().getCookie("refresh_token");
        assertThat(loginRefreshToken).isNotNull();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(loginRefreshToken))
                .andExpect(status().isNoContent())
                .andReturn();

        assertAuthCookies(refreshResult, false);

        Cookie rotatedRefreshToken = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(rotatedRefreshToken).isNotNull();

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(rotatedRefreshToken))
                .andExpect(status().isNoContent())
                .andReturn();

        assertAuthCookies(logoutResult, true);
    }

    @ParameterizedTest(name = "USER cannot access {0}")
    @MethodSource("adminOnlyEndpoints")
    void userShouldBeForbiddenFromEveryAdminOnlyEndpoint(
            String endpoint,
            MockHttpServletRequestBuilder request
    ) throws Exception {
        String email = uniqueEmail("admin-authorization");
        registerUser("Regular User", email, PASSWORD);
        Cookie accessToken = loginAndGetAccessToken(email, PASSWORD);

        mockMvc.perform(request.cookie(accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    private void registerUser(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());
    }

    private Cookie loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("access_token");

        assertThat(accessToken).isNotNull();

        return accessToken;
    }

    private Cookie loginAndGetRefreshToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie refreshToken = result.getResponse().getCookie("refresh_token");

        assertThat(refreshToken).isNotNull();

        return refreshToken;
    }

    private MvcResult refreshConcurrently(
            String refreshTokenValue,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) throws Exception {
        readyLatch.countDown();
        startLatch.await();

        return mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshTokenValue)))
                .andReturn();
    }

    private String createAdminUser() {
        String email = uniqueEmail("admin");

        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        return email;
    }

    private static Stream<Arguments> adminOnlyEndpoints() {
        return Stream.of(
                Arguments.of(
                        "POST /api/resources",
                        post("/api/resources")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                ),
                Arguments.of(
                        "PUT /api/resources/{id}",
                        put("/api/resources/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                ),
                Arguments.of(
                        "DELETE /api/resources/{id}",
                        delete("/api/resources/1")
                ),
                Arguments.of(
                        "POST /api/resources/{id}/slots/generate",
                        post("/api/resources/1/slots/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                ),
                Arguments.of(
                        "POST /api/admin/bookings",
                        post("/api/admin/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                ),
                Arguments.of(
                        "GET /api/admin/stats",
                        get("/api/admin/stats")
                )
        );
    }

    private void assertAuthCookies(MvcResult result, boolean cleared) {
        assertAuthCookie(result, "access_token", "/", cleared);
        assertAuthCookie(result, "refresh_token", "/api/auth", cleared);
    }

    private void assertAuthCookie(
            MvcResult result,
            String name,
            String expectedPath,
            boolean cleared
    ) {
        Cookie cookie = result.getResponse().getCookie(name);
        String setCookieHeader = result.getResponse()
                .getHeaders(HttpHeaders.SET_COOKIE)
                .stream()
                .filter(header -> header.startsWith(name + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Set-Cookie header for " + name));

        assertThat(cookie).isNotNull();
        assertThat(cookie.getPath()).isEqualTo(expectedPath);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isFalse();
        assertThat(setCookieHeader).contains("SameSite=Lax");

        if (cleared) {
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge()).isZero();
        } else {
            assertThat(cookie.getValue()).isNotBlank();
            assertThat(cookie.getMaxAge()).isPositive();
        }
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }
}
