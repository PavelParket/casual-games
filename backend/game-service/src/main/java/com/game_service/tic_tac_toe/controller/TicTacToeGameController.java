package com.game_service.tic_tac_toe.controller;

import com.common_utils.dto.ErrorResponse;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.service.TicTacToeGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game/t-t-t")
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
public class TicTacToeGameController {

    private final TicTacToeGameService ticTacToeGameService;

    @PostMapping("/start")
    @Operation(summary = "Start a new Tic-Tac-Toe match")
    @ApiResponse(responseCode = "200")
    public TicTacToeGameResponse processStart(@RequestBody TicTacToeGameRequest request) {
        return ticTacToeGameService.processStart(request);
    }

    @PostMapping("/move")
    @Operation(summary = "Process a move for the Tic-Tac-Toe match")
    @ApiResponse(responseCode = "200")
    public TicTacToeGameResponse processMove(@RequestBody TicTacToeGameRequest request) {
        return ticTacToeGameService.processMove(request);
    }
}
