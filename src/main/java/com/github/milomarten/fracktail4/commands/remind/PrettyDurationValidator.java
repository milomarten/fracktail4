package com.github.milomarten.fracktail4.commands.remind;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PrettyDurationValidator implements ConstraintValidator<PrettyDuration, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return DurationUtils.isValidFormat(s);
    }
}
