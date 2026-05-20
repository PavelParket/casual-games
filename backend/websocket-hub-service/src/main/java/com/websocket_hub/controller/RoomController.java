package com.websocket_hub.controller;

import com.websocket_hub.domain.dto.request.RoomFilterRequest;
import com.websocket_hub.domain.dto.request.RoomRequest;
import com.websocket_hub.domain.dto.response.PlayerResponse;
import com.websocket_hub.domain.dto.response.RoomResponse;
import com.websocket_hub.domain.dto.response.RoomResponseMap;
import com.websocket_hub.domain.dto.response.RoomStatusResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ws/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/players/{roomId}/{roomType}")
    public Map<UUID, PlayerResponse> getPlayers(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getPlayers(roomId, roomType);
    }

    @GetMapping("/ready-count/{roomId}/{roomType}")
    public Integer getReadyPlayerCount(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getReadyPlayerCount(roomId, roomType);
    }

    @GetMapping("/types")
    public List<RoomType> getTypes() {
        return roomService.getTypes();
    }

    @PostMapping
    public RoomResponse create(@Valid @RequestBody RoomRequest roomRequest) {
        return roomService.create(roomRequest);
    }

    @GetMapping("{id}")
    public RoomResponse getById(@PathVariable UUID id) {
        return roomService.getById(id);
    }

    @GetMapping("status/{roomId}/{roomType}")
    public RoomStatusResponse getStatus(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getStatus(roomId, roomType);
    }

    @PostMapping("/search")
    public RoomResponseMap search(@Valid @RequestBody RoomFilterRequest request) {
        return roomService.search(request);
    }
}
