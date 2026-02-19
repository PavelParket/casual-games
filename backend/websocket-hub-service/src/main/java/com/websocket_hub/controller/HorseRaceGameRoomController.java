package com.websocket_hub.controller;

import com.websocket_hub.domain.entity.HorseRaceGamePreset;
import com.websocket_hub.service.HorseRaceGameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ws/rooms/horse-race")
@RequiredArgsConstructor
public class HorseRaceGameRoomController {

    private final HorseRaceGameRoomService horseRaceGameRoomService;

    @GetMapping("/preset/{roomId}")
    public HorseRaceGamePreset getPreset(@PathVariable UUID roomId) {
        return horseRaceGameRoomService.getPreset(roomId);
    }
}
