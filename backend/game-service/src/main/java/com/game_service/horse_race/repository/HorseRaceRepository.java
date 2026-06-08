package com.game_service.horse_race.repository;

import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HorseRaceRepository extends JpaRepository<HorseRace, Long> {

    Optional<HorseRace> findByRoomId(UUID roomId);

    boolean existsByRoomIdAndStatus(UUID roomId, HorseRaceStatus status);

    @Query(value = """
            SELECT * FROM game_horse_races
            WHERE jsonb_exists(players, CAST(:userGuid AS text))
                AND status = 'FINISHED'
                AND CASE
                    WHEN CAST(:isWinner AS boolean) IS NULL THEN TRUE
                    WHEN CAST(:isWinner AS boolean) IS TRUE THEN
                        CAST(players ->> CAST(:userGuid AS text) AS integer) = winner_horse_index
                    WHEN CAST(:isWinner AS boolean) IS FALSE THEN
                        CAST(players ->> CAST(:userGuid AS text) AS integer) != winner_horse_index
                END
            """,
            countQuery = """
                    SELECT COUNT(*) FROM game_horse_races
                    WHERE jsonb_exists(players, CAST(:userGuid AS text))
                        AND status = 'FINISHED'
                        AND CASE
                            WHEN CAST(:isWinner AS boolean) IS NULL THEN TRUE
                            WHEN CAST(:isWinner AS boolean) IS TRUE THEN
                                CAST(players ->> CAST(:userGuid AS text) AS integer) = winner_horse_index
                            WHEN CAST(:isWinner AS boolean) IS FALSE THEN
                                CAST(players ->> CAST(:userGuid AS text) AS integer) != winner_horse_index
                        END
                    """,
            nativeQuery = true)
    Page<HorseRace> findMatchHistory(UUID userGuid, Boolean isWinner, Pageable pageable);
}
