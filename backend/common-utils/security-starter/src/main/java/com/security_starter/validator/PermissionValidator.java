package com.security_starter.validator;

import com.security_starter.annotation.Permission;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    public boolean can(Permissions permissions, Operation operation, PermissionContext context) {
        if (context == null || context.getRole() == null) {
            return false;
        }

        if (Role.ADMIN.equals(context.getRole())) {
            return true;
        }

        if (Role.USER.equals(context.getRole())) {
            return context.isOwner();
        }

        return false;
    }

    public void readObject(Object object, PermissionContext context) {
        allFields(object.getClass()).forEach(field -> {
            if (field.isAnnotationPresent(Permission.class)) {
                if (!can(field.getAnnotation(Permission.class).value(), Operation.READ, context)) {
                    setNull(field, object);
                }
            }
        });
    }

    public void updateObject(Object source, Object target, PermissionContext context) {
        Field[] sourceFields = source.getClass().getDeclaredFields();
        Field[] targetFields = target.getClass().getDeclaredFields();

        Map<String, Field> targetFieldMap = Arrays.stream(targetFields)
                .collect(Collectors.toMap(
                        Field::getName,
                        field -> field
                ));

        Arrays.stream(sourceFields).forEach(sourceField -> {
            Field targetField = targetFieldMap.get(sourceField.getName());

            if (targetField == null) {
                return;
            }

            Permissions permission = Optional.ofNullable(targetField.getAnnotation(Permission.class))
                    .map(Permission::value)
                    .orElse(null);

            if (permission == null || !can(permission, Operation.UPDATE, context)) {
                return;
            }

            try {
                sourceField.setAccessible(true);
                Object value = sourceField.get(source);

                if (value == null) {
                    return;
                }

                targetField.setAccessible(true);
                targetField.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot update field: " + sourceField.getName(), e);
            }
        });
    }

    private void setNull(Field field, Object object) {
        try {
            field.setAccessible(true);
            field.set(object, null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot sanitize field: " + field.getName(), e);
        }
    }

    private Stream<Field> allFields(Class<?> type) {
        Stream<Field> fields = Arrays.stream(type.getDeclaredFields());
        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            fields = Stream.concat(fields, allFields(type.getSuperclass()));
        }
        return fields;
    }

}
