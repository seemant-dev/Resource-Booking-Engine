package com.resourcebooking.server.exception;

import com.resourcebooking.server.dto.response.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SlotNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSlotNotFound(
            SlotNotFoundException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "SLOT_NOT_FOUND",
                exception.getMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleSlotAlreadyBooked(
            SlotAlreadyBookedException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "SLOT_ALREADY_BOOKED",
                exception.getMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({
            OptimisticLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "SLOT_ALREADY_BOOKED",
                "Slot is no longer available. Please refresh and try again.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                "Request conflicts with existing data.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                message,
                Instant.now(),
                request.getRequestURI()
        );
    }
}