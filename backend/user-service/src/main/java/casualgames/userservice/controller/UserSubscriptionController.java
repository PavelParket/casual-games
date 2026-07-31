package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.SubscriptionRequest;
import casualgames.userservice.domain.dto.SubscriptionResponse;
import casualgames.userservice.service.UserSubscriptionService;
import com.common_utils.dto.ErrorResponse;
import com.security_starter.config.AuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping("/purchase")
    @Operation(summary = "Purchase a subscription plan", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public SubscriptionResponse purchase(@Valid @RequestBody SubscriptionRequest request,
                                         @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.purchase(request, authenticationToken);
    }

    @GetMapping
    @Operation(summary = "Get the current user subscription plan", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public SubscriptionResponse get(@AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.get(authenticationToken);
    }

    @PatchMapping("/auto-renew")
    @Operation(summary = "Enable or disable subscription auto-renewal", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public SubscriptionResponse updateAutoRenew(@RequestParam Boolean enable,
                                                @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userSubscriptionService.updateAutoRenew(enable, authenticationToken);
    }
}
