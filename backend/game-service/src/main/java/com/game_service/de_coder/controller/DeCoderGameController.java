package com.game_service.de_coder.controller;

import com.common_utils.dto.ErrorResponse;
import com.game_service.de_coder.domain.dto.DeCoderGameRequest;
import com.game_service.de_coder.domain.dto.DeCoderGameResponse;
import com.game_service.de_coder.service.DeCoderGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/game/de_coder")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SERVICE')")
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
public class DeCoderGameController {

    private final DeCoderGameService deCoderGameService;

    @PostMapping("/start")
    @Operation(summary = "Start a new De-Coder game")
    @ApiResponse(responseCode = "200")
    public DeCoderGameResponse processStart(@RequestBody DeCoderGameRequest request) {
        return deCoderGameService.processStart(request);
    }

    @PostMapping("/move")
    @Operation(summary = "Process a move in De-Coder game")
    @ApiResponse(responseCode = "200")
    public DeCoderGameResponse processMove(@RequestBody DeCoderGameRequest request) {
        return deCoderGameService.processMove(request);
    }

    @GetMapping("/{roomId}/state")
    @Operation(summary = "Get the current state of a De-Coder game")
    @ApiResponse(responseCode = "200")
    public DeCoderGameResponse getGameState(@PathVariable UUID roomId) {
        return deCoderGameService.getGameState(roomId);
    }
}
