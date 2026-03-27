package com.game_service.tic_tac_toe.repository;

import com.game_service.tic_tac_toe.entity.TicTacToeGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicTacToeGameRepository extends JpaRepository<TicTacToeGame, Long> {
    Optional<TicTacToeGame> findByRoomId(UUID roomId);
}