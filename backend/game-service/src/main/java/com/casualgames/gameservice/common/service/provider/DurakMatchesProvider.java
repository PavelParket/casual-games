package com.casualgames.gameservice.common.service.provider;

import com.casualgames.gameservice.common.dto.GameMatchRequestFilter;
import com.casualgames.gameservice.common.dto.GameMatchResponse;
import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import com.casualgames.gameservice.durak.domain.entity.Durak;
import com.casualgames.gameservice.durak.mapper.DurakGameMapper;
import com.casualgames.gameservice.durak.repository.DurakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DurakMatchesProvider implements GameMatchesProvider {

    private final DurakRepository durakRepository;

    private final DurakGameMapper durakGameMapper;

    @Override
    public GameType gameType() {
        return GameType.DURAK;
    }

    @Override
    public Page<GameMatchResponse> findMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        Page<Durak> page = durakRepository.findMatchHistory(userGuid, gameMatchRequestFilter.isWinner(), pageable);

        return page.map(durak ->
                durakGameMapper.toMatchResponse(
                        durak,
                        userGuid,
                        GameType.DURAK,
                        resolveGameResult(durak, userGuid)
                )
        );
    }

    private GameResult resolveGameResult(Durak durak, UUID userGuid) {
        switch (durak.getStatus()) {
            case DRAW -> {
                return GameResult.DRAW;
            }

            case WINNER -> {
                return userGuid.equals(durak.getWinnerId()) ? GameResult.WIN : GameResult.LOSS;
            }

            default -> {
                return GameResult.NO_DATA;
            }
        }
    }
}
