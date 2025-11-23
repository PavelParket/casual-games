package com.security_service.mapper;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthResponse toResponse(UserResponse user, String accessToken);
}
