package com.security_service.mapper;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest registerRequest);

    @Mapping(target = "role", expression = "java(user.getRole().toString())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "email", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", qualifiedByName = "ignoreEmpty")
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget User user, UpdateRequest updateRequest);

    @Named("ignoreEmpty")
    default String ignoreEmpty(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
