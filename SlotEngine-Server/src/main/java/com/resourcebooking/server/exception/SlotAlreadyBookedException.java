package com.resourcebooking.server.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException(Long id) {
        super("Slot is already booked with id: " + id);
    }
}