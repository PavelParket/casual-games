package com.websocket_hub.controller;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.service.MahjongGameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ws/rooms/mahjong")
@RequiredArgsConstructor
public class MahjongGameRoomController {

    private final MahjongGameRoomService mahjongGameRoomService;

    @GetMapping("/player-bets/{roomId}")
    public List<PlayerBet> getPlayerBets(@PathVariable UUID roomId) {
        return mahjongGameRoomService.getPlayerBets(roomId);
    }
}
