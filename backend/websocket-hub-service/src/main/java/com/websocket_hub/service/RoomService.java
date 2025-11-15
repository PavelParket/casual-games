package com.websocket_hub.service;

import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.GameRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return getManager(GameRoomManager.class)
                .map(manager -> manager.getUserIds(roomName).stream().toList())
                .orElse(List.of());
    }

    public Integer getReadyPlayerCount(String roomName) {
        return getManager(GameRoomManager.class)
                .map(manager -> manager.getReadyPlayerCount(roomName))
                .orElse(0);
    }

    private <T extends AbstractRoomManager> Optional<T> getManager(Class<T> type) {
        return roomManagers.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }
}
