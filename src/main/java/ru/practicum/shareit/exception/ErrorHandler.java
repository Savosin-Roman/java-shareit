package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
        log.warn("ApiException: code={}, args={}", e.getCode(), e.getArgs());
        return ResponseEntity
                .status(e.getCode().getHttpStatus())
                .body(Map.of(
                        "error", e.getCode().getKey(),
                        "message", e.getCode().getKey()
                ));
    }
}