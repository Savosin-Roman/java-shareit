package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final Object[] args;

    public ApiException(ErrorCode code, Object... args) {
        this.code = code;
        this.args = args;
    }
}