package com.websocket_hub.controller;

import com.common_utils.dto.ErrorResponse;
import com.security_starter.config.AuthenticationToken;
import com.websocket_hub.domain.dto.RoomInviteResponseList;
import com.websocket_hub.domain.dto.request.RoomInviteRequest;
import com.websocket_hub.domain.dto.response.RoomInviteResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomInviteService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ws/room-invite")
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
        @ApiResponse(responseCode = "503",
                description = "Service Unavailable",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class RoomInviteController {

    private final RoomInviteService roomInviteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create friend invitation to a room", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201")
    public RoomInviteResponse create(@Valid @RequestBody RoomInviteRequest request,
                                     @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return roomInviteService.create(request, authenticationToken);
    }

    @GetMapping("/{roomId}/{roomType}/users")
    @Operation(summary = "List room invite candidates", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RoomInviteResponseList getInviteUsers(@PathVariable UUID roomId,
                                                 @PathVariable RoomType roomType,
                                                 @ParameterObject @PageableDefault(sort = "username") Pageable pageable,
                                                 @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return roomInviteService.getInviteUsers(roomId, roomType, pageable, authenticationToken);
    }
}
