package com.game_service.durak.repository;

import com.game_service.durak.domain.entity.Durak;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DurakRepository extends JpaRepository<Durak, Long> {

    @Query(value = """
            SELECT * FROM game_durak
            WHERE players @> jsonb_build_array(:userGuid)
                AND CASE
                    WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER','DRAW')
                    WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status = 'WINNER'
                    WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status = 'WINNER'
                END
            """,
            countQuery = """
                    SELECT COUNT(*) FROM game_durak
                    WHERE players @> jsonb_build_array(:userGuid)
                        AND CASE
                            WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER','DRAW')
                            WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status = 'WINNER'
                            WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status = 'WINNER'
                        END
                    """,
            nativeQuery = true)
    Page<Durak> findMatchHistory(UUID userGuid, Boolean isWinner, Pageable pageable);
}
