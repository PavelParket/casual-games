package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.FriendRequestRequest;
import casualgames.userservice.domain.dto.FriendRequestResponse;
import casualgames.userservice.domain.dto.FriendRequestResponseList;
import casualgames.userservice.service.FriendRequestService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/friend-request")
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
        @ApiResponse(responseCode = "409",
                description = "Conflict",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/{recipientGuid}")
    @Operation(summary = "Create a friend request", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public FriendRequestResponse create(@PathVariable UUID recipientGuid,
                                        @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return friendRequestService.create(recipientGuid, authenticationToken);
    }

    @PatchMapping
    @Operation(summary = "Accept, decline or cancel a friend request", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public FriendRequestResponse update(@Valid @RequestBody FriendRequestRequest request,
                                        @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return friendRequestService.update(request, authenticationToken);
    }

    @GetMapping("/search")
    @Operation(summary = "Get incoming or outgoing friend requests", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public FriendRequestResponseList search(@RequestParam(required = false) Boolean incoming,
                                            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                            @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return friendRequestService.search(incoming, pageable, authenticationToken);
    }
}
