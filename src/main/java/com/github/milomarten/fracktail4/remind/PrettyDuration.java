package com.github.milomarten.fracktail4.remind;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = PrettyDurationValidator.class)
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrettyDuration {
    String message() default "Invalid Duration format. ex: 1h30m";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
