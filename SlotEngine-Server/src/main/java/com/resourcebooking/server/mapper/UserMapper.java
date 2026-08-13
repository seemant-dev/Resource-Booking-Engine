package com.resourcebooking.server.mapper;

import com.resourcebooking.server.dto.response.UserResponse;
import com.resourcebooking.server.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}