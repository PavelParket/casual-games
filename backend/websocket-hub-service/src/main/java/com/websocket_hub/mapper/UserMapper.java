package com.websocket_hub.mapper;

import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.websocket_hub.domain.dto.response.UserResponse;
import com.websocket_hub.domain.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget User user, SynchronizedUser synchronizedUser);

    UserResponse toResponse(User user);
}
