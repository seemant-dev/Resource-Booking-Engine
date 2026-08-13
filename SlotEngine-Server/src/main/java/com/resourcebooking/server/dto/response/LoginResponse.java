package com.resourcebooking.server.dto.response;

import com.resourcebooking.server.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Role role;
    private String name;
}