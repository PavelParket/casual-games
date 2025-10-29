package com.game_service.tic_tac_toe.controller;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/t-t-t")
@RequiredArgsConstructor
public class GameController {

    private final GameService service;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse processStart(@RequestBody GameRequest request) {
        return service.processStart(request);
    }

    @PostMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse processMove(@RequestBody GameRequest request) {
        return service.processMove(request);
    }
}
