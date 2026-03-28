package com.websocket_hub.service;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.manager.DurakGameRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DurakGameRoomService {

    public final DurakGameRoomManager durakGameRoomManager;

    public List<PlayerBet> getPlayerBets(UUID roomId) {
        return durakGameRoomManager.getPlayerBets(roomId).stream()
                .map(playerBet -> new PlayerBet(playerBet.getGuid(), playerBet.getBet(), null))
                .toList();
    }
}
