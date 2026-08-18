package com.resourcebooking.server.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

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

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }
}