package com.websocket_hub.service;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.manager.TicTacToeGameRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicTacToeGameRoomService {

    public final TicTacToeGameRoomManager ticTacToeGameRoomManager;

    public List<PlayerBet> getPlayerBets(UUID roomId) {
        return ticTacToeGameRoomManager.getPlayerBets(roomId).stream()
                .map(playerBet -> new PlayerBet(playerBet.getGuid(), playerBet.getBet(), null))
                .toList();
    }
}
