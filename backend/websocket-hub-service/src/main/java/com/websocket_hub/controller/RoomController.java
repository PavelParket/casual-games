package com.websocket_hub.controller;

import com.websocket_hub.domain.dto.client.RoomInfoResponse;
import com.websocket_hub.domain.dto.client.RoomTypeResponse;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ws/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService service;

    @GetMapping("/all")
    public List<RoomInfoResponse> getRooms() {
        return service.getRooms();
    }

    @GetMapping("/{roomName}/players")
    public List<String> getPlayersInRoom(@PathVariable String roomName, @RequestParam RoomType roomType) {
        return service.getPlayerEmailsInRoom(roomName, roomType);
    }

    @GetMapping("/{roomName}/ready-count")
    public Integer getReadyPlayerCount(@PathVariable String roomName, @RequestParam RoomType roomType) {
        return service.getReadyPlayerCount(roomName, roomType);
    }

    @GetMapping("/types")
    public List<RoomTypeResponse> getTypes() {
        return service.getTypes();
    }
}
