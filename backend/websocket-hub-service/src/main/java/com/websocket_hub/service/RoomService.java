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

    public List<String> getRoomsNames() {
        return roomManagers.stream()
                .flatMap(manager -> manager.getActiveRoomsNames().stream())
                .toList();
    }

    public List<String> getPlayersInRoom(String roomName) {
        return roomManagers.stream()
                .filter(manager -> "gameRoomManager".equals(manager.getName()))
                .flatMap(manager -> manager.getUserIds(roomName).stream())
                .toList();
    }
}
