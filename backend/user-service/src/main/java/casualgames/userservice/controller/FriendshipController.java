package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.FriendshipResponseList;
import casualgames.userservice.domain.dto.UserFriendRequestFilter;
import casualgames.userservice.domain.dto.UserFriendResponseList;
import casualgames.userservice.service.FriendshipService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/friends")
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
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/{userGuid}")
    @Operation(summary = "Get user friends", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public FriendshipResponseList getByUserGuid(@PathVariable UUID userGuid,
                                                @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return friendshipService.getByUserGuid(userGuid, pageable, authenticationToken);
    }

    @DeleteMapping("/{friendGuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a friend", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204")
    public void delete(@PathVariable UUID friendGuid, @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        friendshipService.delete(friendGuid, authenticationToken);
    }

    @PostMapping("/search")
    @Operation(summary = "Search users to add as friends", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public UserFriendResponseList search(@Valid @RequestBody UserFriendRequestFilter filter,
                                         @ParameterObject @PageableDefault(sort = "username") Pageable pageable,
                                         @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return friendshipService.search(filter, pageable, authenticationToken);
    }
}
