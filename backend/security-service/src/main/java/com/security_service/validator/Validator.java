package com.security_service.validator;

import com.common_utils.exception.BadRequestException;

public interface Validator {

    default <T> void validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new BadRequestException("Field \"" + fieldName + "\" cannot be null");
        }
    }

    @Deprecated
    default void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException("Field \"" + fieldName + "\" cannot be empty");
        }
    }

    default void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Field \"" + fieldName + "\" cannot be blank");
        }
    }

    default void validateString(String value, String fieldName) {
        validateNotNull(value, fieldName);
        validateNotBlank(value, fieldName);
    }
}
