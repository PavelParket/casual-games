package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.SubscriptionPlanResponse;
import casualgames.userservice.service.SubscriptionPlanService;
import com.security_starter.config.AuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public List<SubscriptionPlanResponse> get(@AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return subscriptionPlanService.get(authenticationToken);
    }
}
