package com.resourcebooking.server.exception;

import com.resourcebooking.server.common.DatabaseConstraints;
import com.resourcebooking.server.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request();

    @ParameterizedTest
    @ValueSource(strings = {
            DatabaseConstraints.SLOTS_RESOURCE_TIME_EXCLUSION,
            DatabaseConstraints.SLOTS_RESOURCE_START_UNIQUE
    })
    void mapsOnlyKnownSlotConstraintsToSlotGenerationConflict(String constraintName) {
        ErrorResponse response = handler.handleDataIntegrityViolation(
                databaseException(constraintName, "23P01"),
                request
        );

        assertThat(response.getError()).isEqualTo("SLOT_GENERATION_CONFLICT");
    }

    @Test
    void doesNotMapUnknownExclusionConstraintToSlotGenerationConflict() {
        ErrorResponse response = handler.handleDataIntegrityViolation(
                databaseException("ex_future_unrelated_rule", "23P01"),
                request
        );

        assertThat(response.getError()).isEqualTo("DATA_INTEGRITY_VIOLATION");
    }

    @Test
    void mapsConfirmedBookingConstraintToSlotAlreadyBooked() {
        ErrorResponse response = handler.handleDataIntegrityViolation(
                databaseException(DatabaseConstraints.BOOKINGS_CONFIRMED_SLOT_UNIQUE, "23505"),
                request
        );

        assertThat(response.getError()).isEqualTo("SLOT_ALREADY_BOOKED");
    }

    private DataIntegrityViolationException databaseException(
            String constraintName,
            String sqlState
    ) {
        PSQLException postgresException = mock(PSQLException.class);
        ServerErrorMessage serverError = mock(ServerErrorMessage.class);
        when(postgresException.getSQLState()).thenReturn(sqlState);
        when(postgresException.getServerErrorMessage()).thenReturn(serverError);
        when(serverError.getConstraint()).thenReturn(constraintName);

        return new DataIntegrityViolationException("Database constraint violation", postgresException);
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/resources/1/slots/generate");
        return request;
    }
}
