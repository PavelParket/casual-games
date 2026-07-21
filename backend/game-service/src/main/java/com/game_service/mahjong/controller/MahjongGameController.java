package com.game_service.mahjong.controller;

import com.game_service.mahjong.domain.dto.MahjongGameRequest;
import com.game_service.mahjong.domain.dto.MahjongGameResponse;
import com.game_service.mahjong.service.MahjongGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("game/mahjong")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SERVICE')")
public class MahjongGameController {

    private final MahjongGameService mahjongGameService;

    @PostMapping("/start")
    public MahjongGameResponse processStart(@RequestBody MahjongGameRequest request) {
        return mahjongGameService.processStart(request);
    }

    @PostMapping("/move")
    public MahjongGameResponse processMove(@RequestBody MahjongGameRequest request) {
        return mahjongGameService.processMove(request);
    }

    @PostMapping("/finish")
    public void processFinish(@RequestBody MahjongGameRequest request) {
        mahjongGameService.processFinish(request);
    }
}
