package com.resourcebooking.server.config.properties;

import com.resourcebooking.server.enums.LockingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    private LockingStrategy lockingStrategy = LockingStrategy.OPTIMISTIC;
}