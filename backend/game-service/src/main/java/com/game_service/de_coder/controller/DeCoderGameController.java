package com.game_service.de_coder.controller;

import com.game_service.de_coder.dto.DeCoderGameRequest;
import com.game_service.de_coder.dto.DeCoderGameResponse;
import com.game_service.de_coder.service.DeCoderGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game/de_coder")
@RequiredArgsConstructor
public class DeCoderGameController {

    private final DeCoderGameService deCoderGameService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public DeCoderGameResponse processStart(@RequestBody DeCoderGameRequest request) {
        return deCoderGameService.processStart(request);
    }

    @PostMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public DeCoderGameResponse processMove(@RequestBody DeCoderGameRequest request) {
        return deCoderGameService.processMove(request);
    }
}
