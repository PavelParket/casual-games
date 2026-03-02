package com.game_service.horse_race.repository;

import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HorseRaceRepository extends JpaRepository<HorseRace, Long> {

    Optional<HorseRace> findByRoomId(UUID roomId);

    boolean existsByRoomIdAndStatus(UUID roomId, HorseRaceStatus status);
}
