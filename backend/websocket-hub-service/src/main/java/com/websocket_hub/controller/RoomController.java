package com.websocket_hub.controller;

import com.websocket_hub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ws/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService service;

    @GetMapping("/all")
    public List<String> getRoomsNames() {
        return service.getRoomsNames();
    }
}
