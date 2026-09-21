package com.casualgames.userservice.mapper;

import com.casualgames.userservice.domain.dto.UserFriendResponse;
import com.casualgames.userservice.domain.dto.UserResponse;
import com.casualgames.userservice.domain.entity.User;
import com.casualgames.userservice.domain.enums.FriendshipStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toListResponse(List<User> users);

    UserFriendResponse toResponse(User user, FriendshipStatus friendshipStatus);
}
