package com.websocket_hub.controller;

import com.common_utils.dto.ErrorResponse;
import com.websocket_hub.domain.dto.request.RoomFilterRequest;
import com.websocket_hub.domain.dto.request.RoomRequest;
import com.websocket_hub.domain.dto.response.PlayerResponse;
import com.websocket_hub.domain.dto.response.RoomResponse;
import com.websocket_hub.domain.dto.response.RoomResponseMap;
import com.websocket_hub.domain.dto.response.RoomStatusResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ws/rooms")
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
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/players/{roomId}/{roomType}")
    @Operation(summary = "Get map of active players inside a room", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public Map<UUID, PlayerResponse> getPlayers(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getPlayers(roomId, roomType);
    }

    @GetMapping("/ready-count/{roomId}/{roomType}")
    @Operation(summary = "Get count of players who are ready in the room", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public Integer getReadyPlayerCount(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getReadyPlayerCount(roomId, roomType);
    }

    @GetMapping("/types")
    @Operation(summary = "Get list of all supported game room types", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<RoomType> getTypes() {
        return roomService.getTypes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new game room", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201")
    public RoomResponse create(@Valid @RequestBody RoomRequest roomRequest) {
        return roomService.create(roomRequest);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get basic room data by id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RoomResponse getById(@PathVariable UUID id) {
        return roomService.getById(id);
    }

    @GetMapping("status/{roomId}/{roomType}")
    @Operation(summary = "Get comprehensive game room live status", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RoomStatusResponse getStatus(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getStatus(roomId, roomType);
    }

    @PostMapping("/search")
    @Operation(summary = "Search available game rooms by filter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RoomResponseMap search(@Valid @RequestBody RoomFilterRequest request) {
        return roomService.search(request);
    }
}
