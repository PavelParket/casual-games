package com.websocket_hub.controller;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.domain.dto.RoomTypeResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ws/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/all")
    public List<RoomResponse> getRooms() {
        return roomService.getRooms();
    }

    @GetMapping("/{roomName}/players")
    public List<String> getPlayersInRoom(@PathVariable String roomName, @RequestParam RoomType roomType) {
        return roomService.getPlayerEmailsInRoom(roomName, roomType);
    }

    @GetMapping("/{roomName}/ready-count")
    public Integer getReadyPlayerCount(@PathVariable String roomName, @RequestParam RoomType roomType) {
        return roomService.getReadyPlayerCount(roomName, roomType);
    }

    @GetMapping("/types")
    public List<RoomTypeResponse> getTypes() {
        return roomService.getTypes();
    }

    @PostMapping
    public RoomResponse create(@RequestBody RoomRequest roomRequest) {
        return roomService.create(roomRequest);
    }
}
