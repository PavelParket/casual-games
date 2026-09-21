package com.casualgames.userservice.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.userservice.domain.dto.FriendRequestResponse;
import com.casualgames.userservice.domain.dto.UserResponse;
import com.casualgames.userservice.domain.entity.FriendRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendRequestMapper extends PagedModelMapper {

    @Mapping(target = "status", source = "friendRequest.status")
    @Mapping(target = "createdAt", source = "friendRequest.createdAt")
    FriendRequestResponse toResponse(FriendRequest friendRequest, UserResponse requester, UserResponse recipient);
}
