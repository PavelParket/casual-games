package com.casualgames.securityservice.mapper;

import com.casualgames.securityservice.domain.dto.AuthResponse;
import com.casualgames.securityservice.domain.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthResponse toResponse(UserResponse user, String accessToken);
}
