package com.casualgames.userservice.domain.dto;

import com.casualgames.securitystarter.enums.Status;
import com.casualgames.userservice.domain.enums.FriendshipStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserFriendResponse(

        UUID guid,

        String username,

        Status status,

        String linkProfilePicture,

        String linkProfilePictureMini,

        FriendshipStatus friendshipStatus
) {
}
