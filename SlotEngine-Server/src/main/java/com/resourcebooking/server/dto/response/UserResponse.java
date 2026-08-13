package com.resourcebooking.server.dto.response;

import com.resourcebooking.server.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Instant createdAt;
}