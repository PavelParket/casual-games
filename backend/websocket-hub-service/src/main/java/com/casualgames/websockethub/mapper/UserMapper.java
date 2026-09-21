package com.casualgames.websockethub.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.kafkastarter.dto.event.sync.SynchronizedUser;
import com.casualgames.websockethub.domain.dto.response.UserResponse;
import com.casualgames.websockethub.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper extends PagedModelMapper {

    void updateEntity(@MappingTarget User user, SynchronizedUser synchronizedUser);

    UserResponse toResponse(User user);
}
