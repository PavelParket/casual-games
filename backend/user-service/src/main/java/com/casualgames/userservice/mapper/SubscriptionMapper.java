package com.casualgames.userservice.mapper;

import com.casualgames.securitystarter.enums.Status;
import com.casualgames.userservice.domain.dto.SubscriptionResponse;
import com.casualgames.userservice.domain.entity.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(source = "currentStatus", target = "status")
    SubscriptionResponse toResponse(UserSubscription subscription, Status currentStatus);
}
