package com.resourcebooking.server.exception;

public class InvalidSlotGenerationRequestException extends RuntimeException {

    public InvalidSlotGenerationRequestException(String message) {
        super(message);
    }
}
