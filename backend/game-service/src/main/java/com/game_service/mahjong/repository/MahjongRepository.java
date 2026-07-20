package com.game_service.mahjong.repository;

import com.game_service.mahjong.domain.entity.Mahjong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MahjongRepository extends JpaRepository<Mahjong, Long> {

    Optional<Mahjong> findByRoomId(UUID roomId);

    @Query(value = """
            SELECT * FROM game_mahjong
            WHERE players @> jsonb_build_array(:userGuid)
                AND CASE
                    WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER','DRAW')
                    WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status = 'WINNER'
                    WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status = 'WINNER'
                END
            """,
            countQuery = """
                    SELECT COUNT(*) FROM game_mahjong
                    WHERE players @> jsonb_build_array(:userGuid)
                        AND CASE
                            WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER','DRAW')
                            WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status = 'WINNER'
                            WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status = 'WINNER'
                        END
                    """,
            nativeQuery = true)
    Page<Mahjong> findMatchHistory(UUID userGuid, Boolean isWinner, Pageable pageable);
}
