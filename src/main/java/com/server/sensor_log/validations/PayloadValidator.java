package com.server.sensor_log.validations;

import org.springframework.stereotype.Component;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayloadValidator {

    private final Validator validator;

    public <T> void validateOrThrow(T obj) {
        var violations = validator.validate(obj);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> String.format(
                    "field '%s': %s (value: %s)",
                    v.getPropertyPath(),
                    v.getMessage(),
                    v.getInvalidValue()
            ))
                    .toList()
                    .toString();

            log.error("🔴 Validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
}
