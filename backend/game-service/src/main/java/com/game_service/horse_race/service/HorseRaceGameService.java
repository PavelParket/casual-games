package com.game_service.horse_race.service;

import com.game_service.common.exception.GameValidationException;
import com.game_service.horse_race.domain.dto.HorseRaceGamePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceGameRequest;
import com.game_service.horse_race.domain.dto.HorseRaceGameResponse;
import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.entity.HorseRaceHorseKeyframes;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import com.game_service.horse_race.factory.HorseRaceFactory;
import com.game_service.horse_race.mapper.HorseRaceMapper;
import com.game_service.horse_race.repository.HorseRaceRepository;
import com.game_service.horse_race.util.HorseRaceGameUtils;
import com.game_service.horse_race.validator.HorseRaceValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.game_service.horse_race.util.HorseRaceGameUtils.SEGMENTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorseRaceGameService {

    private final HorseRaceRepository horseRaceRepository;

    private final HorseRaceMapper horseRaceMapper;

    private final HorseRaceValidator horseRaceValidator;

    private final HorseRaceFactory horseRaceFactory;

    public HorseRaceGamePresetResponse processCreate(HorseRaceGameRequest request) {
        horseRaceValidator.validateCreate(request);

        Integer horseCount = HorseRaceGameUtils.calculateHorseCount();
        List<Double> odds = HorseRaceGameUtils.calculateOdds(horseCount);

        log.info("Created preset for room={}: horseCount={}, odds={}", request.roomId(), horseCount, odds);

        return horseRaceMapper.toPresetResponse(request.roomId(), horseCount, odds);
    }

    @Transactional
    public HorseRaceGameResponse processStart(HorseRaceGameRequest request) {
        horseRaceValidator.validateStart(request);

        Integer horseCount = request.horseCount();
        Integer segmentsCount = SEGMENTS;

        String serverSeed = UUID.randomUUID().toString();
        String seedHash = HorseRaceGameUtils.calculateHash(serverSeed);

        Random seededRandom = new Random(serverSeed.hashCode());

        Integer[][] speeds = HorseRaceGameUtils.buildSpeeds(seededRandom, horseCount, segmentsCount);
        List<Double> totalDistances = HorseRaceGameUtils.buildTotalDistances(speeds, horseCount, segmentsCount);
        Integer winnerHorseIndex = HorseRaceGameUtils.findWinner(totalDistances, horseCount);

        List<HorseRaceHorseKeyframes> horseKeyframes = HorseRaceGameUtils.buildKeyFrames(
                speeds,
                totalDistances,
                horseCount,
                segmentsCount
        );

        HorseRace horseRace = horseRaceRepository.save(horseRaceFactory.create(
                request.roomId(),
                serverSeed,
                seedHash,
                horseCount,
                winnerHorseIndex,
                segmentsCount
        ));

        log.info("Race running for room={}: winner=horse#{}, segments={}, seed={}", request.roomId(), winnerHorseIndex, segmentsCount, seedHash);

        return horseRaceMapper.toResponse(
                horseRace,
                HorseRaceEvent.START,
                HorseRaceGameUtils.calculateOdds(horseCount),
                horseKeyframes
        );
    }

    @Transactional
    public void processResult(HorseRaceGameRequest request) {
        horseRaceValidator.validateResult(request);

        HorseRace race = horseRaceRepository.findByRoomId(request.roomId())
                .orElseThrow(() -> new GameValidationException("Race not found for room=" + request.roomId()));

        race.setStatus(HorseRaceStatus.FINISHED);

        horseRaceRepository.save(race);

        log.info("Race finished for room={}", request.roomId());
    }
}
