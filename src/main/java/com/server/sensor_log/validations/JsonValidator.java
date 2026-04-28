package com.server.sensor_log.validations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class JsonValidator implements ConstraintValidator<ValidJson, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            objectMapper.readTree(value);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
