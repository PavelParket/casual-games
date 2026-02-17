package com.game_service.horse_race.validator;

import com.game_service.horse_race.domain.dto.HorseRaceRequest;
import com.game_service.horse_race.util.HorseRaceGameUtils;
import com.game_service.tic_tac_toe.exception.GameValidationException;
import org.springframework.stereotype.Component;

@Component
public class HorseRaceValidator {

    public void validateCreate(HorseRaceRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room id cannot be null");
        }
    }

    public void validateStart(HorseRaceRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room id cannot be null");
        }

        if (request.participants() == null || request.participants().isEmpty()) {
            throw new GameValidationException("Participants cannot be null or empty");
        }

        if (request.horseCount() == null) {
            throw new GameValidationException("Horse count cannot be null");
        }

        if (request.horseCount() < HorseRaceGameUtils.MIN_HORSES
                || request.horseCount() > HorseRaceGameUtils.MAX_HORSES) {
            throw new GameValidationException(
                    "Horse count must be between "
                            + HorseRaceGameUtils.MIN_HORSES
                            + " and "
                            + HorseRaceGameUtils.MAX_HORSES
                            + ", got: "
                            + request.horseCount()
            );
        }
    }

    public void validateResult(HorseRaceRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room id cannot be null");
        }
    }
}
