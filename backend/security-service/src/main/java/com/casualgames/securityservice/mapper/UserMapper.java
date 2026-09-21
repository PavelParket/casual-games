package com.casualgames.securityservice.mapper;

import com.casualgames.kafkastarter.dto.event.sync.SynchronizedUser;
import com.casualgames.securityservice.domain.dto.RegisterRequest;
import com.casualgames.securityservice.domain.dto.UserResponse;
import com.casualgames.securityservice.domain.entity.User;
import com.casualgames.securitystarter.enums.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {Role.class, UUID.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", expression = "java(UUID.randomUUID())")
    @Mapping(target = "role", expression = "java(Role.USER)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", source = "password")
    User toEntity(RegisterRequest registerRequest, String password);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget User user, SynchronizedUser synchronizedUser);
}
