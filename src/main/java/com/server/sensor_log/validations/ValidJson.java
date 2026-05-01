package com.server.sensor_log.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonValidator.class)
@Documented
public @interface ValidJson {

    String message() default "🔴 Error: Invalid JSON Format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
