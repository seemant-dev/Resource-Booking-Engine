package com.resourcebooking.server.exception;

import com.resourcebooking.server.common.DatabaseConstraints;
import com.resourcebooking.server.dto.response.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBookingNotFound(
            BookingNotFoundException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "BOOKING_NOT_FOUND",
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
        return slotAlreadyBookedResponse(exception.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Invalid email or password.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleRefreshTokenException(
            InvalidRefreshTokenException exception,
            HttpServletRequest request
    ) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "REFRESH_TOKEN_INVALID",
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
        return slotAlreadyBookedResponse(
                "Slot is no longer available. Please refresh and try again.",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        if (isBookingConfirmedSlotConstraintViolation(exception)) {
            return slotAlreadyBookedResponse(
                    "Slot is already booked.",
                    request
            );
        }

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

    private ErrorResponse slotAlreadyBookedResponse(String message, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "SLOT_ALREADY_BOOKED",
                message,
                Instant.now(),
                request.getRequestURI()
        );
    }

    private boolean isBookingConfirmedSlotConstraintViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();

        return message != null
                && message.contains(DatabaseConstraints.BOOKINGS_CONFIRMED_SLOT_UNIQUE);
    }
}