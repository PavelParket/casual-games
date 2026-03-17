package com.game_service.durak.controller;

import com.game_service.durak.domain.dto.DurakGameRequest;
import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.service.DurakGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/durak")
@RequiredArgsConstructor
public class DurakGameController {

    private final DurakGameService durakGameService;

    @PostMapping("/start")
    public DurakGameResponse processStart(@RequestBody DurakGameRequest request) {
        return durakGameService.processStart(request);
    }

    @PostMapping("/move")
    public DurakGameResponse processMove(@RequestBody DurakGameRequest request) {
        return durakGameService.processMove(request);
    }

    @PostMapping("/timeout")
    public DurakGameResponse processTimeout(@RequestBody DurakGameRequest request) {
        return durakGameService.processTimeout(request);
    }

    /*@GetMapping("/{id}")
    public DurakGameResponse getById(@PathVariable Long id) {
        return durakGameService
    }*/
}
