package com.notifications.controller;

import com.common_utils.dto.ErrorResponse;
import com.notifications.domain.dto.AnnouncementCreateRequest;
import com.notifications.domain.dto.AnnouncementResponse;
import com.notifications.domain.dto.AnnouncementResponseList;
import com.notifications.service.AnnouncementService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcements")
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
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an announcement", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201")
    public AnnouncementResponse create(@Valid @RequestBody AnnouncementCreateRequest request,
                                       @AuthenticationPrincipal AuthenticationToken token) {
        return announcementService.create(request, token);
    }

    @GetMapping
    @Operation(summary = "Get announcements", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public AnnouncementResponseList getAnnouncements(@ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                     @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return announcementService.getAnnouncements(pageable, authenticationToken);
    }

    @PatchMapping("/mark-as-seen")
    @Operation(summary = "Mark announcements as seen", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public AnnouncementResponseList markAsSeen(@ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                               @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return announcementService.markAsSeen(pageable, authenticationToken);
    }
}
