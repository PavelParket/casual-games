package com.game_service.mahjong.service;

import com.game_service.common.enums.GameType;
import com.game_service.common.exception.NotFoundException;
import com.game_service.common.service.provider.GameCleanupProvider;
import com.game_service.mahjong.domain.dto.MahjongGameRequest;
import com.game_service.mahjong.domain.dto.MahjongGameResponse;
import com.game_service.mahjong.domain.entity.Board;
import com.game_service.mahjong.domain.entity.Mahjong;
import com.game_service.mahjong.domain.enums.MahjongStatus;
import com.game_service.mahjong.mapper.MahjongGameMapper;
import com.game_service.mahjong.repository.MahjongGameRedisRepository;
import com.game_service.mahjong.repository.MahjongRepository;
import com.game_service.mahjong.util.MahjongGameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.game_service.config.ResourceMessageConstants.MAHJONG_GAME_NOT_FOUND;
import static com.game_service.config.ResourceMessageConstants.MAHJONG_MATCH_NOT_FOUND;
import static com.game_service.config.ResourceMessageConstants.MAHJONG_PLAYER_NOT_IN_GAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class MahjongGameService implements GameCleanupProvider {

    private static final int ZERO = 0;

    private final MahjongRepository mahjongRepository;

    private final MahjongGameRedisRepository mahjongGameRedisRepository;

    private final MahjongGameMapper mahjongGameMapper;

    @Override
    public GameType gameType() {
        return GameType.MAHJONG;
    }

    @Override
    @Transactional
    public void cleanup(UUID roomId) {
        mahjongGameRedisRepository.delete(roomId);

        mahjongRepository.findByRoomId(roomId).ifPresent(match -> {
            if (match.getStatus() == MahjongStatus.STARTED) {
                match.setStatus(MahjongStatus.CANCELLED);
                mahjongRepository.save(match);
            }
        });
    }

    @Transactional
    public MahjongGameResponse processStart(MahjongGameRequest request) {
        long seed = ThreadLocalRandom.current().nextLong();

        Mahjong mahjong = MahjongGameUtils.initGame(seed, request.players());

        mahjongGameRedisRepository.save(request.roomId(), mahjong);

        Mahjong match = Mahjong.builder()
                .roomId(request.roomId())
                .status(MahjongStatus.STARTED)
                .players(request.players())
                .seed(seed)
                .build();

        mahjongRepository.save(match);

        return mahjongGameMapper.toStartResponse(request.roomId(), seed, mahjongGameMapper.toBoardResponseList(mahjong.getBoards()));
    }

    public MahjongGameResponse processMove(MahjongGameRequest request) {
        Mahjong mahjong = mahjongGameRedisRepository.find(request.roomId())
                .orElseThrow(() -> new NotFoundException(MAHJONG_GAME_NOT_FOUND));

        Board board = mahjong.getBoards().get(request.playerGuid());

        if (board == null) {
            throw new NotFoundException(MAHJONG_PLAYER_NOT_IN_GAME);
        }

        boolean valid = MahjongGameUtils.removePair(board, request.slot1(), request.slot2());

        mahjongGameRedisRepository.save(request.roomId(), mahjong);

        UUID opponentGuid = resolveOpponent(mahjong, request.playerGuid());
        Board opponentBoard = opponentGuid != null ? mahjong.getBoards().get(opponentGuid) : null;
        List<String> removedSlotIds = valid ? List.of(request.slot1(), request.slot2()) : List.of();
        int opponentTilesRemaining = opponentBoard != null ? opponentBoard.getRemaining() : ZERO;

        return mahjongGameMapper.toMoveResponse(
                request.roomId(),
                request.playerGuid(),
                valid,
                removedSlotIds,
                board.getRemaining(),
                MahjongGameUtils.availableMoves(board),
                MahjongGameUtils.isCleared(board),
                MahjongGameUtils.isDeadlocked(board),
                opponentGuid,
                opponentTilesRemaining
        );
    }

    @Transactional
    public void processFinish(MahjongGameRequest request) {
        Mahjong match = mahjongRepository.findByRoomId(request.roomId())
                .orElseThrow(() -> new NotFoundException(MAHJONG_MATCH_NOT_FOUND));

        match.setStatus(request.winnerId() != null ? MahjongStatus.WINNER : MahjongStatus.DRAW);
        match.setWinnerId(request.winnerId());
        match.setTilesCleared(request.tilesCleared());

        mahjongRepository.save(match);
    }

    private UUID resolveOpponent(Mahjong game, UUID playerGuid) {
        return game.getBoards().keySet().stream()
                .filter(guid -> !guid.equals(playerGuid))
                .findFirst()
                .orElse(null);
    }
}
