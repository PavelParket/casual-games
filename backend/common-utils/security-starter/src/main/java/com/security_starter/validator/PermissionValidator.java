package com.security_starter.validator;

import com.security_starter.annotation.Permission;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.OperationPostfix;
import com.security_starter.enums.Permissions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.security_starter.enums.Operation.CREATE;
import static com.security_starter.enums.Operation.DELETE;
import static com.security_starter.enums.Operation.READ;
import static com.security_starter.enums.Operation.UPDATE;
import static com.security_starter.enums.OperationPostfix.CREATE_FOR_ALL;
import static com.security_starter.enums.OperationPostfix.CREATE_FOR_ME;
import static com.security_starter.enums.OperationPostfix.CREATE_WITHOUT_ME;
import static com.security_starter.enums.OperationPostfix.DELETE_FOR_ALL;
import static com.security_starter.enums.OperationPostfix.DELETE_FOR_ME;
import static com.security_starter.enums.OperationPostfix.DELETE_WITHOUT_ME;
import static com.security_starter.enums.OperationPostfix.FOR_ALL;
import static com.security_starter.enums.OperationPostfix.FOR_ME;
import static com.security_starter.enums.OperationPostfix.READ_FOR_ALL;
import static com.security_starter.enums.OperationPostfix.READ_FOR_ME;
import static com.security_starter.enums.OperationPostfix.READ_WITHOUT_ME;
import static com.security_starter.enums.OperationPostfix.UPDATE_FOR_ALL;
import static com.security_starter.enums.OperationPostfix.UPDATE_FOR_ME;
import static com.security_starter.enums.OperationPostfix.UPDATE_WITHOUT_ME;
import static com.security_starter.enums.OperationPostfix.WITHOUT_ME;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    public static final String UNDERSCORE = "_";

    private static final Map<Operation, Set<String>> permissionMap = Map.of(
            CREATE, Set.of(CREATE_FOR_ME.name(), CREATE_FOR_ALL.name(), CREATE_WITHOUT_ME.name()),
            READ, Set.of(READ_FOR_ME.name(), READ_FOR_ALL.name(), READ_WITHOUT_ME.name()),
            UPDATE, Set.of(UPDATE_FOR_ME.name(), UPDATE_FOR_ALL.name(), UPDATE_WITHOUT_ME.name()),
            DELETE, Set.of(DELETE_FOR_ME.name(), DELETE_FOR_ALL.name(), DELETE_WITHOUT_ME.name())
    );

    public List<String> getPermissions(Permissions permission, Operation operation) {
        return permissionMap.getOrDefault(operation, Collections.emptySet()).stream()
                .map(postfix -> String.join(UNDERSCORE, permission.name(), postfix))
                .collect(Collectors.toList());
    }

    public String getPermission(Permissions permission, Operation operation, OperationPostfix operationPostfix) {
        return permissionMap.getOrDefault(operation, Collections.emptySet()).stream()
                .filter(postfix -> postfix.equals(operationPostfix.name()))
                .map(postfix -> String.join(UNDERSCORE, permission.name(), postfix))
                .findFirst()
                .orElse(null);
    }

    public boolean can(Permissions permission, Operation operation, PermissionContext context, AuthenticationToken token) {
        if (context == null || token == null) {
            return false;
        }

        return getPermissions(permission, operation).stream()
                .anyMatch(p -> checkAccessForAll(p, context, token));
    }

    public boolean checkPermissionByOperation(Set<String> permissions, Permissions permission, Operation operation) {
        return getPermissions(permission, operation).stream()
                .anyMatch(permissions::contains);
    }

    public void readObject(Object object, PermissionContext context, AuthenticationToken token) {
        allFields(object.getClass()).forEach(field -> {
            if (field.isAnnotationPresent(Permission.class)) {
                if (!can(field.getAnnotation(Permission.class).value(), Operation.READ, context, token)) {
                    setNull(field, object);
                }
            }
        });
    }

    public void updateObject(Object target, Object source, PermissionContext context, AuthenticationToken token) {
        Map<String, Field> sourceFieldMap = Arrays.stream(source.getClass().getDeclaredFields())
                .collect(Collectors.toMap(
                        Field::getName,
                        field -> field
                ));

        allFields(target.getClass()).forEach(targetField -> {
            Field sourceField = sourceFieldMap.get(targetField.getName());

            if (sourceField == null) {
                return;
            }

            Permission permission = targetField.getAnnotation(Permission.class);

            if (permission == null) {
                copyField(sourceField, source, targetField, target);
                return;
            }

            if (!can(permission.value(), UPDATE, context, token)) {
                return;
            }

            swapValueFields(
                    sourceField, source,
                    targetField, target,
                    permission.createAllowed(),
                    permission.editAllowed(),
                    permission.deleteAllowed()
            );
        });
    }

    private boolean checkAccessForAll(String permission, PermissionContext context, AuthenticationToken token) {
        if (!token.getPermissions().contains(permission)) {
            return false;
        }

        return permission.endsWith(FOR_ALL.name())
                || (permission.endsWith(FOR_ME.name()) && context.isOwner())
                || (permission.endsWith(WITHOUT_ME.name()) && !context.isOwner());
    }

    private void swapValueFields(Field sourceField,
                                 Object source,
                                 Field targetField,
                                 Object target,
                                 boolean createAllowed,
                                 boolean editAllowed,
                                 boolean deleteAllowed) {
        try {
            sourceField.setAccessible(true);
            targetField.setAccessible(true);

            Object sourceValue = sourceField.get(source);
            Object targetValue = targetField.get(target);

            if (targetValue == null && sourceValue != null && !createAllowed) {
                return;
            }

            if (targetValue != null && sourceValue != null
                    && !targetValue.equals(sourceValue) && !editAllowed) {
                return;
            }

            if (targetValue != null && sourceValue == null && !deleteAllowed) {
                return;
            }

            targetField.set(target, sourceValue);

        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot update field: " + sourceField.getName(), e);
        }
    }

    private void copyField(Field sourceField, Object source, Field targetField, Object target) {
        try {
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            targetField.set(target, sourceField.get(source));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot copy field: " + sourceField.getName(), e);
        }
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
