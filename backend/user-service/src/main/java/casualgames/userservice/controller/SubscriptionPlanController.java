package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.SubscriptionPlanResponse;
import casualgames.userservice.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final UserSubscriptionService userSubscriptionService;

    @GetMapping("/all")
    public List<SubscriptionPlanResponse> getAll() {
        return userSubscriptionService.getPlans();
    }
}
