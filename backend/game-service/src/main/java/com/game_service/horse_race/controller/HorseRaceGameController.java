package com.game_service.horse_race.controller;

import com.common_utils.dto.ErrorResponse;
import com.game_service.horse_race.domain.dto.HorseRaceGamePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceGameRequest;
import com.game_service.horse_race.domain.dto.HorseRaceGameResponse;
import com.game_service.horse_race.service.HorseRaceGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class HorseRaceGameController {

    private final HorseRaceGameService horseRaceGameService;

    @PostMapping("/create")
    @Operation(summary = "Create a Horse-Race game room preset")
    @ApiResponse(responseCode = "200")
    public HorseRaceGamePresetResponse processCreate(@RequestBody HorseRaceGameRequest request) {
        return horseRaceGameService.processCreate(request);
    }

    @PostMapping("/start")
    @Operation(summary = "Start a Horse-Race match")
    @ApiResponse(responseCode = "200")
    public HorseRaceGameResponse processStart(@RequestBody HorseRaceGameRequest request) {
        return horseRaceGameService.processStart(request);
    }

    @PostMapping("/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Process results for a Horse-Race match")
    @ApiResponse(responseCode = "200")
    public void processResult(@RequestBody HorseRaceGameRequest request) {
        horseRaceGameService.processResult(request);
    }
}
