package com.game_service.tic_tac_toe.repository;

import com.game_service.tic_tac_toe.domain.entity.TicTacToe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicTacToeGameRepository extends JpaRepository<TicTacToe, Long> {

    Optional<TicTacToe> findByRoomId(UUID roomId);

    @Query(value = """
            SELECT * FROM game_tic_tac_toe
            WHERE jsonb_exists(players, CAST(:userGuid AS text))
                AND CASE
                    WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER_X','WINNER_O','DRAW')
                    WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status IN ('WINNER_X','WINNER_O')
                    WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status IN ('WINNER_X','WINNER_O')
                END
            """,
            countQuery = """
                    SELECT COUNT(*) FROM game_tic_tac_toe
                    WHERE jsonb_exists(players, CAST(:userGuid AS text))
                        AND CASE
                            WHEN CAST(:isWinner AS boolean) IS NULL THEN status IN ('WINNER_X','WINNER_O','DRAW')
                            WHEN CAST(:isWinner AS boolean) IS TRUE THEN winner_id = :userGuid AND status IN ('WINNER_X','WINNER_O')
                            WHEN CAST(:isWinner AS boolean) IS FALSE THEN winner_id != :userGuid AND status IN ('WINNER_X','WINNER_O')
                        END
                    """,
            nativeQuery = true)
    Page<TicTacToe> findMatchHistory(UUID userGuid, Boolean isWinner, Pageable pageable);
}