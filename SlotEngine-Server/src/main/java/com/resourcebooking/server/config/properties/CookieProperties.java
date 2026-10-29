package com.resourcebooking.server.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.security.cookie")
public class CookieProperties {

    private boolean secure;

    @NotBlank
    @Pattern(
            regexp = "Strict|Lax|None",
            message = "must be one of Strict, Lax, or None"
    )
    private String sameSite;

    @NotBlank
    private String accessTokenName;

    @NotBlank
    private String refreshTokenName;

    @NotBlank
    private String accessTokenPath;

    @NotBlank
    private String refreshTokenPath;
}
