package com.mycompany.bachhoaxanhonline.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

public class ValidationUtil {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static Validator getValidator() {
        return validator;
    }

    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    public static <T> String getFirstViolationMessage(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (violations != null && !violations.isEmpty()) {
            return violations.iterator().next().getMessage();
        }
        return null;
    }
}
