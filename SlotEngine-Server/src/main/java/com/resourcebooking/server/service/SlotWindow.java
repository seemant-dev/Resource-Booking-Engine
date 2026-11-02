package com.resourcebooking.server.service;

import java.time.Instant;

public record SlotWindow(Instant start, Instant end) {
}
