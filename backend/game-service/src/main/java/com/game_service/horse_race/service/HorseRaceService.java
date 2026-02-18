package com.game_service.horse_race.service;

import com.game_service.common.enums.MessageType;
import com.game_service.common.exception.GameValidationException;
import com.game_service.horse_race.domain.dto.HorseRacePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceRequest;
import com.game_service.horse_race.domain.dto.HorseRaceResponse;
import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.entity.HorseTick;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class HorseRaceService {

    private final HorseRaceRepository horseRaceRepository;

    private final HorseRaceMapper horseRaceMapper;

    private final HorseRaceValidator horseRaceValidator;

    public HorseRacePresetResponse processCreate(HorseRaceRequest request) {
        log.info("Processing CREATE for room={}", request.roomId());

        horseRaceValidator.validateCreate(request);

        int horseCount = HorseRaceGameUtils.calculateHorseCount();
        double[] odds = HorseRaceGameUtils.calculateOdds(horseCount);

        log.info("Created preset for room={}: horseCount={}, odds={}", request.roomId(), horseCount, odds);

        return horseRaceMapper.toPresetResponse(
                MessageType.SYSTEM,
                request.roomId(),
                "Race preset created",
                horseCount,
                odds
        );
    }

    @Transactional
    public HorseRaceResponse processStart(HorseRaceRequest request) {
        log.info("Processing START for room={}, horseCount={}", request.roomId(), request.horseCount());

        horseRaceValidator.validateStart(request);

        int horseCount = request.horseCount();
        int segmentsCount = HorseRaceGameUtils.calculateSegmentsCount();

        String serverSeed = UUID.randomUUID().toString();
        String seedHash = HorseRaceGameUtils.calculateHash(serverSeed);

        Random seededRandom = new Random(serverSeed.hashCode());

        int[][] speeds = HorseRaceGameUtils.buildSpeeds(seededRandom, horseCount, segmentsCount);
        double[] totalDistances = HorseRaceGameUtils.buildTotalDistances(speeds, horseCount, segmentsCount);
        int winnerHorseIndex = HorseRaceGameUtils.findWinner(totalDistances, horseCount);

        List<HorseTick> ticks = HorseRaceGameUtils.buildTicks(speeds, totalDistances, horseCount, segmentsCount);

        HorseRace horseRace = horseRaceMapper.toEntity(
                request.roomId(),
                serverSeed,
                seedHash,
                horseCount,
                winnerHorseIndex,
                segmentsCount,
                HorseRaceStatus.RUNNING
        );

        horseRaceRepository.save(horseRace);

        log.info("Race simulated for room={}: winner=horse#{}, segments={}, seed={}", request.roomId(), winnerHorseIndex, segmentsCount, seedHash);

        return horseRaceMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.START,
                request.roomId(),
                "Race started",
                seedHash,
                serverSeed,
                horseCount,
                HorseRaceGameUtils.calculateOdds(horseCount),
                winnerHorseIndex,
                segmentsCount,
                ticks
        );
    }

    @Transactional
    public void processResult(HorseRaceRequest request) {
        log.info("Processing RESULT for room={}", request.roomId());

        horseRaceValidator.validateResult(request);

        HorseRace race = horseRaceRepository.findByRoomId(request.roomId())
                .orElseThrow(() -> new GameValidationException("Race not found for room=" + request.roomId()));

        race.setStatus(HorseRaceStatus.FINISHED);

        horseRaceRepository.save(race);

        log.info("Race finished for room={}", request.roomId());
    }
}
