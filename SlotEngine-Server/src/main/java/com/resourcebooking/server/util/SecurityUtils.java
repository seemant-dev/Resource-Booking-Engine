package com.resourcebooking.server.util;

import com.resourcebooking.server.enums.Role;
import com.resourcebooking.server.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        return getCurrentUserPrincipal().getId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUserPrincipal().getRole();
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated user not found");
        }

        return principal;
    }
}