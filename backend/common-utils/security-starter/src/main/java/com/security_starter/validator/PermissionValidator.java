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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public static final String DELIMITER_UNDERSCORE = "_";

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static final Map<Operation, Set<String>> permissionMap = Map.of(
            CREATE, Set.of(CREATE_FOR_ME.name(), CREATE_FOR_ALL.name(), CREATE_WITHOUT_ME.name()),
            READ, Set.of(READ_FOR_ME.name(), READ_FOR_ALL.name(), READ_WITHOUT_ME.name()),
            UPDATE, Set.of(UPDATE_FOR_ME.name(), UPDATE_FOR_ALL.name(), UPDATE_WITHOUT_ME.name()),
            DELETE, Set.of(DELETE_FOR_ME.name(), DELETE_FOR_ALL.name(), DELETE_WITHOUT_ME.name())
    );

    public List<String> getPermissions(Permissions permission, Operation operation) {
        return permissionMap.getOrDefault(operation, Collections.emptySet())
                .stream()
                .map(postfix -> String.join(DELIMITER_UNDERSCORE, permission.name(), postfix))
                .collect(Collectors.toList());
    }

    public String getPermission(Permissions permission, Operation operation, OperationPostfix operationPostfix) {
        return permissionMap.getOrDefault(operation, Collections.emptySet())
                .stream()
                .filter(postfix -> postfix.equals(operationPostfix.name()))
                .map(postfix -> String.join(DELIMITER_UNDERSCORE, permission.name(), postfix))
                .findFirst()
                .orElse(null);
    }

    public boolean hasAccess(Permissions permission, Operation operation, PermissionContext context, AuthenticationToken token) {
        if (context == null || token == null) {
            return false;
        }

        return getPermissions(permission, operation).stream()
                .anyMatch(p -> hasAnyAccessByPermission(p, context, token));
    }

    public void readObject(Object object, PermissionContext context, AuthenticationToken token) {
        allFields(object.getClass()).forEach(field -> {
            Permission annotation = field.getAnnotation(Permission.class);

            if (annotation == null) {
                return;
            }

            boolean allowed = annotation.value() == Permissions.NONE
                    ? hasPrivateAccess(context)
                    : hasAccess(annotation.value(), Operation.READ, context, token);

            if (!allowed) {
                setNull(field, object);
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

            Permission annotation = targetField.getAnnotation(Permission.class);

            if (annotation == null) {
                copyField(sourceField, source, targetField, target);
                return;
            }

            boolean allowed = annotation.value() == Permissions.NONE
                    ? hasPrivateAccess(context)
                    : hasAccess(annotation.value(), Operation.UPDATE, context, token);

            if (allowed) {
                copyField(sourceField, source, targetField, target);
            }
        });
    }

    private boolean hasPrivateAccess(PermissionContext context) {
        return context != null && (context.isOwner() || context.isAdmin());
    }

    private boolean hasAnyAccessByPermission(String permission, PermissionContext context, AuthenticationToken token) {
        if (!token.getPermissions().contains(permission)) {
            return false;
        }

        return permission.endsWith(FOR_ALL.name())
                || (permission.endsWith(FOR_ME.name()) && context.isOwner())
                || (permission.endsWith(WITHOUT_ME.name()) && !context.isOwner());
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

    private List<Field> allFields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, t -> {
            List<Field> fields = new ArrayList<>(
                    Arrays.asList(t.getDeclaredFields())
            );

            if (t.getSuperclass() != null && t.getSuperclass() != Object.class) {
                fields.addAll(
                        allFields(t.getSuperclass())
                );
            }

            fields.forEach(field -> field.setAccessible(true));

            return fields;
        });
    }
}
