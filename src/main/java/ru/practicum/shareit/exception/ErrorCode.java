package ru.practicum.shareit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("user.notFound", HttpStatus.NOT_FOUND.value()),
    EMAIL_ALREADY_EXISTS("user.emailAlreadyExists", HttpStatus.CONFLICT.value()),
    ITEM_NOT_FOUND("item.notFound", HttpStatus.NOT_FOUND.value()),
    ACCESS_DENIED("item.accessDenied", HttpStatus.FORBIDDEN.value()),
    INSERT_FAILED("error.insertFailed", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    UPDATE_FAILED("error.updateFailed", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    DELETE_FAILED("error.deleteFailed", HttpStatus.INTERNAL_SERVER_ERROR.value()),

    BOOKING_NOT_FOUND("booking.notFound", HttpStatus.NOT_FOUND.value()),
    BOOKING_NOT_AVAILABLE("booking.notAvailable", HttpStatus.BAD_REQUEST.value()),
    BOOKING_ACCESS_DENIED("booking.accessDenied", HttpStatus.FORBIDDEN.value()),
    BOOKING_INVALID_DATES("booking.invalidDates", HttpStatus.BAD_REQUEST.value()),
    BOOKING_STATUS_INVALID("booking.statusInvalid", HttpStatus.BAD_REQUEST.value());

    private final String key;
    private final int httpStatus;

    ErrorCode(String key, int httpStatus) {
        this.key = key;
        this.httpStatus = httpStatus;
    }
}