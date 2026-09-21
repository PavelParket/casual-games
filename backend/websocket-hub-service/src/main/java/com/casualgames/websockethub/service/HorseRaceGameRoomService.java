package com.casualgames.websockethub.service;

import com.casualgames.websockethub.domain.entity.HorseRaceGamePreset;
import com.casualgames.websockethub.manager.HorseRaceGameRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorseRaceGameRoomService {

    private final HorseRaceGameRoomManager horseRaceGameRoomManager;

    public HorseRaceGamePreset getPreset(UUID roomId) {
        HorseRaceGamePreset preset = horseRaceGameRoomManager.getPreset(roomId);

        if (preset == null) {
            log.error("Preset not found for room={}", roomId);
            throw new RuntimeException("Preset not found for room: " + roomId);
        }

        log.info("Retrieved preset for room={}: horseCount={}", roomId, preset.horseCount());

        return preset;
    }
}
