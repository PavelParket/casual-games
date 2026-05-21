package casualgames.userservice.controller;

import casualgames.userservice.dto.SubscriptionRequest;
import casualgames.userservice.dto.SubscriptionResponse;
import casualgames.userservice.service.UserSubscriptionService;
import casualgames.userservice.service.scheduler.SubscriptionScheduler;
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

    private final UserSubscriptionService subscriptionService;

    // todo: delete!!!
    private final SubscriptionScheduler subscriptionScheduler;

    @PostMapping("/purchase")
    public SubscriptionResponse purchase(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.purchase(request);
    }

    @GetMapping
    public SubscriptionResponse get() {
        return subscriptionService.get();
    }

    @PatchMapping("/auto-renew")
    public SubscriptionResponse updateAutoRenew(@RequestParam Boolean enable) {
        return subscriptionService.updateAutoRenew(enable);
    }

    @PostMapping("/update-subscriptions")
    public void process() {
        subscriptionScheduler.run();
    }
}
