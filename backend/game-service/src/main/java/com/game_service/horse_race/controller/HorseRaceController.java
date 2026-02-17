package com.game_service.horse_race.controller;

import com.game_service.horse_race.domain.dto.HorseRacePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceRequest;
import com.game_service.horse_race.domain.dto.HorseRaceResponse;
import com.game_service.horse_race.service.HorseRaceService;
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
public class HorseRaceController {

    private final HorseRaceService horseRaceService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public HorseRacePresetResponse processCreate(@RequestBody HorseRaceRequest request) {
        return horseRaceService.processCreate(request);
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public HorseRaceResponse processStart(@RequestBody HorseRaceRequest request) {
        return horseRaceService.processStart(request);
    }

    @PostMapping("/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processResult(@RequestBody HorseRaceRequest request) {
        horseRaceService.processResult(request);
    }
}
