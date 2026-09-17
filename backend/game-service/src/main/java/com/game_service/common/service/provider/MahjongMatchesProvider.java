package com.game_service.common.service.provider;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import com.game_service.mahjong.domain.entity.Mahjong;
import com.game_service.mahjong.mapper.MahjongGameMapper;
import com.game_service.mahjong.repository.MahjongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MahjongMatchesProvider implements GameMatchesProvider {

    private final MahjongRepository mahjongRepository;

    private final MahjongGameMapper mahjongGameMapper;

    @Override
    public GameType gameType() {
        return GameType.MAHJONG;
    }

    @Override
    public Page<GameMatchResponse> findMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        Page<Mahjong> page = mahjongRepository.findMatchHistory(userGuid, gameMatchRequestFilter.isWinner(), pageable);

        return page.map(mahjong ->
                mahjongGameMapper.toMatchResponse(
                        mahjong,
                        userGuid,
                        GameType.MAHJONG,
                        resolveGameResult(mahjong, userGuid)
                )
        );
    }

    private GameResult resolveGameResult(Mahjong mahjong, UUID userGuid) {
        switch (mahjong.getStatus()) {
            case DRAW -> {
                return GameResult.DRAW;
            }

            case WINNER -> {
                return userGuid.equals(mahjong.getWinnerId()) ? GameResult.WIN : GameResult.LOSS;
            }

            default -> {
                return GameResult.NO_DATA;
            }
        }
    }
}
