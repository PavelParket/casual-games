package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.SubscriptionRequest;
import casualgames.userservice.domain.dto.SubscriptionResponse;
import casualgames.userservice.service.UserSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public SubscriptionResponse purchase(@Valid @RequestBody SubscriptionRequest request) {
        return userSubscriptionService.purchase(request);
    }

    @GetMapping
    public SubscriptionResponse get() {
        return userSubscriptionService.get();
    }

    @PatchMapping("/auto-renew")
    public SubscriptionResponse updateAutoRenew(@RequestParam Boolean enable) {
        return userSubscriptionService.updateAutoRenew(enable);
    }
}
