package com.websocket_hub.controller;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomService;
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

    @GetMapping("/all")
    public List<RoomResponse> getRooms() {
        return roomService.getRooms();
    }

    @GetMapping("/type/{roomType}")
    public List<RoomResponse> getRoomsByType(@PathVariable RoomType roomType) {
        return roomService.getRoomsByType(roomType);
    }

    @GetMapping("/players/{roomId}/{roomType}")
    public Map<UUID, String> getUsernamesInRoom(@PathVariable UUID roomId, @PathVariable RoomType roomType) {
        return roomService.getUsernamesInRoom(roomId, roomType);
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
    public RoomResponse create(@RequestBody RoomRequest roomRequest) {
        return roomService.create(roomRequest);
    }

    @GetMapping("{id}")
    public RoomResponse getById(@PathVariable UUID id) {
        return roomService.getById(id);
    }
}
