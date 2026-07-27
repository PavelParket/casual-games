package com.game_service.de_coder.controller;

import com.game_service.de_coder.domain.dto.DeCoderGameRequest;
import com.game_service.de_coder.domain.dto.DeCoderGameResponse;
import com.game_service.de_coder.service.DeCoderGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/game/de_coder")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SERVICE')")
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

    @GetMapping("/{roomId}/state")
    @ResponseStatus(HttpStatus.OK)
    public DeCoderGameResponse getGameState(@PathVariable UUID roomId) {
        return deCoderGameService.getGameState(roomId);
    }
}
