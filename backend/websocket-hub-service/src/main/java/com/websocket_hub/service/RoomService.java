package com.websocket_hub.service;

import com.websocket_hub.manager.AbstractRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final List<AbstractRoomManager> roomManagers;

    public List<String> getRooms() {
        return roomManagers.stream()
                .flatMap(manager -> manager.getActiveRooms().stream())
                .toList();
    }
}
