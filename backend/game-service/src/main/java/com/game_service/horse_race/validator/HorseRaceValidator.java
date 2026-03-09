package com.game_service.horse_race.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.horse_race.domain.dto.HorseRaceGameRequest;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import com.game_service.horse_race.util.HorseRaceGameUtils;
import org.springframework.stereotype.Component;

import static com.game_service.config.ResourceMessageConstants.HORSE_RACE_HORSE_COUNT_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.HORSE_RACE_HORSE_COUNT_OUT_OF_RANGE;
import static com.game_service.config.ResourceMessageConstants.HORSE_RACE_PARTICIPANTS_CANNOT_BE_EMPTY;
import static com.game_service.config.ResourceMessageConstants.HORSE_RACE_ROOM_ID_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.HORSE_RACE_WRONG_EVENT;
import static com.game_service.config.ResourceMessageConstants.REQUEST_CANNOT_BE_NULL;

@Component
public class HorseRaceValidator {

    public void validateCreate(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(HORSE_RACE_ROOM_ID_CANNOT_BE_NULL);
        }
    }

    public void validateStart(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (!HorseRaceEvent.START.equals(request.event())) {
            throw new GameValidationException(HORSE_RACE_WRONG_EVENT);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(HORSE_RACE_ROOM_ID_CANNOT_BE_NULL);
        }

        if (request.participants() == null || request.participants().isEmpty()) {
            throw new GameValidationException(HORSE_RACE_PARTICIPANTS_CANNOT_BE_EMPTY);
        }

        if (request.horseCount() == null) {
            throw new GameValidationException(HORSE_RACE_HORSE_COUNT_CANNOT_BE_NULL);
        }

        if (request.horseCount() < HorseRaceGameUtils.MIN_HORSES
                || request.horseCount() > HorseRaceGameUtils.MAX_HORSES) {
            throw new GameValidationException(String.format(
                    HORSE_RACE_HORSE_COUNT_OUT_OF_RANGE,
                    HorseRaceGameUtils.MIN_HORSES,
                    HorseRaceGameUtils.MAX_HORSES,
                    request.horseCount()
            ));
        }
    }

    public void validateResult(HorseRaceGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (!HorseRaceEvent.RESULT.equals(request.event())) {
            throw new GameValidationException(HORSE_RACE_WRONG_EVENT);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(HORSE_RACE_ROOM_ID_CANNOT_BE_NULL);
        }
    }
}
