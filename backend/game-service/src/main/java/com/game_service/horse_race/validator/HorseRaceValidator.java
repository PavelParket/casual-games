package com.game_service.horse_race.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.horse_race.domain.dto.HorseRaceGameRequest;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import com.game_service.horse_race.util.HorseRaceGameUtils;
import org.springframework.stereotype.Component;

@Component
public class HorseRaceValidator {

    public void validateCreate(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room id cannot be null");
        }
    }

    public void validateStart(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (!HorseRaceEvent.START.equals(request.event())) {
            throw new GameValidationException("Wrong game event");
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

    public void validateResult(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (!HorseRaceEvent.RESULT.equals(request.event())) {
            throw new GameValidationException("Wrong game event");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room id cannot be null");
        }
    }
}
