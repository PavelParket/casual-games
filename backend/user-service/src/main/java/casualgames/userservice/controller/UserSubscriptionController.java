package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.SubscriptionRequest;
import casualgames.userservice.domain.dto.SubscriptionResponse;
import casualgames.userservice.service.UserSubscriptionService;
import com.security_starter.config.AuthenticationToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping("/purchase")
    public SubscriptionResponse purchase(@Valid @RequestBody SubscriptionRequest request,
                                         @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.purchase(request, authenticationToken);
    }

    @GetMapping
    public SubscriptionResponse get(@AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.get(authenticationToken);
    }

    @PatchMapping("/auto-renew")
    public SubscriptionResponse updateAutoRenew(@RequestParam Boolean enable,
                                                @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.updateAutoRenew(enable, authenticationToken);
    }
}
