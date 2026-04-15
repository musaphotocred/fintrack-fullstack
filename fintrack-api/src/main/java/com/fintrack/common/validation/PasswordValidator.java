package com.fintrack.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for the @ValidPassword annotation.
 * Enforces: min 8 chars, at least 1 uppercase, 1 digit, 1 special character.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        boolean valid = true;
        StringBuilder message = new StringBuilder();

        if (password.length() < MIN_LENGTH) {
            message.append("Password must be at least 8 characters. ");
            valid = false;
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            message.append("Password must contain at least one uppercase letter. ");
            valid = false;
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            message.append("Password must contain at least one digit. ");
            valid = false;
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            message.append("Password must contain at least one special character. ");
            valid = false;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message.toString().trim())
                    .addConstraintViolation();
        }

        return valid;
    }
}
