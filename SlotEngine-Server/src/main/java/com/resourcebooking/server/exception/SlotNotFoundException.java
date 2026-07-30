package com.resourcebooking.server.exception;

public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(Long id) {
        super("Slot not found with id: " + id);
    }
}