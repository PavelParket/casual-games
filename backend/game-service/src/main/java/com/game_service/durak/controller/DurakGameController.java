package com.game_service.durak.controller;

import com.common_utils.dto.ErrorResponse;
import com.game_service.durak.domain.dto.DurakGameRequest;
import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.service.DurakGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/durak")
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
public class DurakGameController {

    private final DurakGameService durakGameService;

    @PostMapping("/start")
    @Operation(summary = "Start a Durak game match")
    @ApiResponse(responseCode = "200")
    public DurakGameResponse processStart(@RequestBody DurakGameRequest request) {
        return durakGameService.processStart(request);
    }

    @PostMapping("/move")
    @Operation(summary = "Process a move in a Durak game")
    @ApiResponse(responseCode = "200")
    public DurakGameResponse processMove(@RequestBody DurakGameRequest request) {
        return durakGameService.processMove(request);
    }

    @PostMapping("/timeout")
    @Operation(summary = "Process turn timeout execution in Durak game")
    @ApiResponse(responseCode = "200")
    public DurakGameResponse processTimeout(@RequestBody DurakGameRequest request) {
        return durakGameService.processTimeout(request);
    }
}
