package com.security_service.mapper;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.dto.user_service.CreateUserInternalRequest;
import com.security_service.domain.entity.User;
import com.security_service.service.PasswordService;
import com.security_starter.enums.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {Role.class, UUID.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", expression = "java(UUID.randomUUID())")
    @Mapping(target = "password", expression = "java(setPassword(registerRequest.password(), null, passwordService))")
    @Mapping(target = "role", expression = "java(Role.USER)")
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest registerRequest, @Context PasswordService passwordService);

    @Mapping(target = "role", expression = "java(user.getRole().toString())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "username", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "email", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "password", expression = "java(setPassword(updateRequest.password(), user.getPassword(), passwordService))")
    @Mapping(target = "role", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget User user, UpdateRequest updateRequest, @Context PasswordService passwordService);

    @Named("ignoreEmpty")
    default String ignoreEmpty(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    @Named("setPassword")
    default String setPassword(String password, String currentPassword, @Context PasswordService passwordService) {
        if (password == null || password.isBlank()) {
            return currentPassword;
        }

        return passwordService.encode(password);
    }

    CreateUserInternalRequest toCreateUserRequest(User user);
}
