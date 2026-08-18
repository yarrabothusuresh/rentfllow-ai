package com.rentflow.ai.exception;

import com.rentflow.ai.dto.BookingUnavailableDTO;

public class BookingUnavailableException extends RuntimeException {
    private final BookingUnavailableDTO errorDetails;

    public BookingUnavailableException(BookingUnavailableDTO errorDetails) {
        super(errorDetails.getMessage());
        this.errorDetails = errorDetails;
    }

    public BookingUnavailableDTO getErrorDetails() {
        return errorDetails;
    }
}
