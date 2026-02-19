package com.game_service.tic_tac_toe.controller;

import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.service.TicTacToeGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/t-t-t")
@RequiredArgsConstructor
public class TicTacToeGameController {

    private final TicTacToeGameService ticTacToeGameService;

    @PostMapping("/start")
    public TicTacToeGameResponse processStart(@RequestBody TicTacToeGameRequest request) {
        return ticTacToeGameService.processStart(request);
    }

    @PostMapping("/move")
    public TicTacToeGameResponse processMove(@RequestBody TicTacToeGameRequest request) {
        return ticTacToeGameService.processMove(request);
    }
}
