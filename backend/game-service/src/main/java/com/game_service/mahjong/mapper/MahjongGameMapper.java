package com.game_service.mahjong.mapper;

import com.game_service.mahjong.domain.dto.MahjongBoardResponse;
import com.game_service.mahjong.domain.dto.MahjongGameResponse;
import com.game_service.mahjong.domain.dto.MahjongTileResponse;
import com.game_service.mahjong.domain.entity.Board;
import com.game_service.mahjong.domain.entity.TileFace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MahjongGameMapper {

    default MahjongBoardResponse toBoardResponse(UUID playerGuid, Board board) {
        List<MahjongTileResponse> tiles = board.getFaces().entrySet().stream()
                .map(entry -> toTileResponse(entry.getKey(), entry.getValue()))
                .toList();

        return MahjongBoardResponse.builder()
                .playerGuid(playerGuid)
                .tiles(tiles)
                .build();
    }

    default MahjongTileResponse toTileResponse(String slotId, TileFace face) {
        return MahjongTileResponse.builder()
                .slotId(slotId)
                .suit(face.getSuit().name())
                .value(face.getValue())
                .build();
    }

    default List<MahjongBoardResponse> toBoardResponses(Map<UUID, Board> boards) {
        return boards.entrySet().stream()
                .map(entry -> toBoardResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Mapping(target = "valid", ignore = true)
    @Mapping(target = "tilesRemaining", ignore = true)
    @Mapping(target = "removedSlotIds", ignore = true)
    @Mapping(target = "playerGuid", ignore = true)
    @Mapping(target = "opponentTilesRemaining", ignore = true)
    @Mapping(target = "opponentGuid", ignore = true)
    @Mapping(target = "deadlocked", ignore = true)
    @Mapping(target = "cleared", ignore = true)
    @Mapping(target = "availableMoves", ignore = true)
    MahjongGameResponse toStartResponse(UUID roomId, long seed, List<MahjongBoardResponse> boards);

    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "boards", ignore = true)
    MahjongGameResponse toMoveResponse(UUID roomId,
                                       UUID playerGuid,
                                       boolean valid,
                                       List<String> removedSlotIds,
                                       int tilesRemaining,
                                       int availableMoves,
                                       boolean cleared,
                                       boolean deadlocked,
                                       UUID opponentGuid,
                                       int opponentTilesRemaining);
}
