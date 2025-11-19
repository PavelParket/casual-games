package com.game_service.de_coder.controller;

import com.game_service.de_coder.dto.GameRequest;
import com.game_service.de_coder.dto.GameResponse;
import com.game_service.de_coder.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController("deCoderController")
@RequestMapping("/game/de_coder")
@RequiredArgsConstructor
public class GameController {

    private final GameService service;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse processStart(@Valid @RequestBody GameRequest request) {
        return service.processStart(request);
    }

    @PostMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse processMove(@Valid @RequestBody GameRequest request) {
        return service.processMove(request);
    }
}
