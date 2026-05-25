package casualgames.userservice.mapper;

import casualgames.userservice.dto.SubscriptionPlanResponse;
import casualgames.userservice.entity.SubscriptionPlan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    SubscriptionPlanResponse toResponse(SubscriptionPlan subscriptionPlan);

    List<SubscriptionPlanResponse> toResponseList(List<SubscriptionPlan> subscriptionPlans);
}
