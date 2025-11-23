package com.security_service.validator;

public interface Validator {

    default <T> void validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Field \"" + fieldName + "\" cannot be null");
        }
    }

    @Deprecated
    default void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Field \"" + fieldName + "\" cannot be empty");
        }
    }

    default void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field \"" + fieldName + "\" cannot be blank");
        }
    }

    default void validateString(String value, String fieldName) {
        validateNotNull(value, fieldName);
        validateNotBlank(value, fieldName);
    }
}
