package casualgames.userservice.mapper;

import casualgames.userservice.domain.dto.SubscriptionResponse;
import casualgames.userservice.domain.entity.UserSubscription;
import com.security_starter.enums.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(source = "currentStatus", target = "status")
    SubscriptionResponse toResponse(UserSubscription subscription, Status currentStatus);
}
