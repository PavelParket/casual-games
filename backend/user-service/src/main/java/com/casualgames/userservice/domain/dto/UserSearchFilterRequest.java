package com.casualgames.userservice.domain.dto;

import com.casualgames.securitystarter.enums.Status;
import lombok.Builder;

@Builder
public record UserSearchFilterRequest(

        String username,

        Status status
) {
}
