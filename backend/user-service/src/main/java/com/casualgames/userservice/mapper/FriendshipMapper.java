package com.casualgames.userservice.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.userservice.domain.dto.FriendshipResponse;
import com.casualgames.userservice.domain.dto.UserResponse;
import com.casualgames.userservice.domain.entity.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendshipMapper extends PagedModelMapper {

    @Mapping(target = "friendshipDate", source = "friendship.createdAt")
    FriendshipResponse toResponse(Friendship friendship, UserResponse user, UserResponse friend);
}
