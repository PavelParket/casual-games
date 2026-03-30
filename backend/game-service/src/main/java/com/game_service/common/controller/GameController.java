package com.game_service.common.controller;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/history/{userGuid}")
    public Page<GameMatchResponse> getMatches(@PathVariable UUID userGuid,
                                              @RequestBody @Valid GameMatchRequestFilter gameMatchRequestFilter,
                                              @PageableDefault Pageable pageable) {
        return gameService.getMatches(userGuid, gameMatchRequestFilter, pageable);
    }
}
