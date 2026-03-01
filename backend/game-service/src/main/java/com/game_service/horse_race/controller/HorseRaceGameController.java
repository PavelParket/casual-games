package com.game_service.horse_race.controller;

import com.game_service.horse_race.domain.dto.HorseRaceGamePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceGameRequest;
import com.game_service.horse_race.domain.dto.HorseRaceGameResponse;
import com.game_service.horse_race.service.HorseRaceGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/horse-race")
@RequiredArgsConstructor
public class HorseRaceGameController {

    private final HorseRaceGameService horseRaceGameService;

    @PostMapping("/create")
    public HorseRaceGamePresetResponse processCreate(@RequestBody HorseRaceGameRequest request) {
        return horseRaceGameService.processCreate(request);
    }

    @PostMapping("/start")
    public HorseRaceGameResponse processStart(@RequestBody HorseRaceGameRequest request) {
        return horseRaceGameService.processStart(request);
    }

    @PostMapping("/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processResult(@RequestBody HorseRaceGameRequest request) {
        horseRaceGameService.processResult(request);
    }
}
