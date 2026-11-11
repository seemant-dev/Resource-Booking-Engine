package com.resourcebooking.server.integration.resource;

import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.entity.User;
import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.integration.AbstractBookingApiIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminResourceApiIntegrationTest extends AbstractBookingApiIntegrationTest {

    private Cookie adminAccessToken;

    @BeforeEach
    void setUpAdmin() throws Exception {
        User admin = createUser("Resource API Admin", Role.ADMIN);
        adminAccessToken = login(admin);
    }

    @Test
    void createResourceWithBlankNameShouldReturnBadRequestWithoutPersistingResource()
            throws Exception {
        mockMvc.perform(post("/api/resources")
                        .cookie(adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", " ",
                                "description", "Invalid resource"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        assertThat(resourceRepository.count()).isZero();
    }

    @Test
    void updateResourceShouldPersistAndReturnUpdatedValues() throws Exception {
        Resource resource = createResource("Original Resource Name");

        mockMvc.perform(put("/api/resources/{id}", resource.getId())
                        .cookie(adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Updated Resource Name",
                                "description", "Updated resource description"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resource.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Updated Resource Name"))
                .andExpect(jsonPath("$.description").value("Updated resource description"));

        Resource updatedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertThat(updatedResource.getName()).isEqualTo("Updated Resource Name");
        assertThat(updatedResource.getDescription()).isEqualTo("Updated resource description");
    }

    @Test
    void updateMissingResourceShouldReturnNotFoundWithoutPersistingResource() throws Exception {
        mockMvc.perform(put("/api/resources/{id}", 999_999L)
                        .cookie(adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Missing Resource",
                                "description", "Resource does not exist"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        assertThat(resourceRepository.count()).isZero();
    }

    @Test
    void deleteResourceShouldRemoveItFromDatabase() throws Exception {
        Resource resource = createResource("Resource To Delete");

        mockMvc.perform(delete("/api/resources/{id}", resource.getId())
                        .cookie(adminAccessToken))
                .andExpect(status().isNoContent());

        assertThat(resourceRepository.findById(resource.getId())).isEmpty();
    }

    @Test
    void deleteMissingResourceShouldReturnNotFoundWithoutChangingDatabase() throws Exception {
        Resource retainedResource = createResource("Retained Resource");

        mockMvc.perform(delete("/api/resources/{id}", 999_999L)
                        .cookie(adminAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        assertThat(resourceRepository.findAll())
                .extracting(Resource::getId)
                .containsExactly(retainedResource.getId());
    }
}
