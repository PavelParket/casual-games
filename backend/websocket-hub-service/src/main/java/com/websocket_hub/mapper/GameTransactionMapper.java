package com.websocket_hub.mapper;

import com.casualgames.grpc.transaction.DeCoderPlayerTransaction;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.DurakTransactionRequest;
import com.casualgames.grpc.transaction.HorseRacePlayerBetMessage;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.websocket_hub.domain.entity.DecoderPlayerSpending;
import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface GameTransactionMapper {

    default PlayerBetMessage toPlayerBetMessage(PlayerBet bet) {
        return PlayerBetMessage.newBuilder()
                .setGuid(bet.getGuid().toString())
                .setBet(bet.getBet().toPlainString())
                .setBalanceBefore(bet.getBalanceBefore().toPlainString())
                .build();
    }

    default List<PlayerBetMessage> toPlayerBetMessages(List<PlayerBet> bets) {
        return bets.stream()
                .map(this::toPlayerBetMessage)
                .toList();
    }

    default HorseRacePlayerBetMessage toHorseRacePlayerBetMessage(HorseRacePlayerBet bet) {
        return HorseRacePlayerBetMessage.newBuilder()
                .setGuid(bet.getGuid().toString())
                .setHorseIndex(bet.getHorseIndex())
                .setOdd(bet.getOdd())
                .setAmount(bet.getAmount().toPlainString())
                .setBalanceBefore(bet.getBalanceBefore().toPlainString())
                .build();
    }

    default List<HorseRacePlayerBetMessage> toHorseRacePlayerBetMessages(Collection<HorseRacePlayerBet> bets) {
        return bets.stream()
                .map(this::toHorseRacePlayerBetMessage)
                .toList();
    }

    default TicTacToeTransactionRequest toTicTacToeRequest(UUID roomId,
                                                           RoomType roomType,
                                                           List<PlayerBet> playerBets,
                                                           UUID winner) {
        TicTacToeTransactionRequest.Builder builder = TicTacToeTransactionRequest.newBuilder()
                .setRoomId(roomId.toString())
                .setRoomType(roomType.name())
                .addAllPlayerBets(toPlayerBetMessages(playerBets));

        if (winner != null) {
            builder.setWinner(winner.toString());
        }

        return builder.build();
    }

    default HorseRaceTransactionRequest toHorseRaceRequest(UUID roomId,
                                                           RoomType roomType,
                                                           Integer winnerHorseIndex,
                                                           Collection<HorseRacePlayerBet> playerBets) {
        return HorseRaceTransactionRequest.newBuilder()
                .setRoomId(roomId.toString())
                .setRoomType(roomType.name())
                .setWinnerHorseIndex(winnerHorseIndex)
                .addAllPlayerBets(toHorseRacePlayerBetMessages(playerBets))
                .build();
    }

    default DurakTransactionRequest toDurakRequest(UUID roomId,
                                                   RoomType roomType,
                                                   List<PlayerBet> playerBets,
                                                   UUID winner) {
        DurakTransactionRequest.Builder builder = DurakTransactionRequest.newBuilder()
                .setRoomId(roomId.toString())
                .setRoomType(roomType.name())
                .addAllPlayerBets(toPlayerBetMessages(playerBets));

        if (winner != null) {
            builder.setWinner(winner.toString());
        }

        return builder.build();
    }

    default DeCoderTransactionRequest toDeCoderRequest(UUID roomId,
                                                       RoomType roomType,
                                                       List<DecoderPlayerSpending> playerSpendingList,
                                                       UUID winnerGuid,
                                                       BigDecimal jackpot) {
        List<DeCoderPlayerTransaction> entries = playerSpendingList.stream()
                .map(spending -> {
                    DeCoderPlayerTransaction.Builder builder = DeCoderPlayerTransaction.newBuilder()
                            .setGuid(spending.getUserGuid().toString())
                            .setBalanceBefore(spending.getBalanceBefore().toPlainString())
                            .setSpent(spending.getSpent().toPlainString());

                    if (spending.getUserGuid().equals(winnerGuid) && jackpot != null) {
                        builder.setJackpot(jackpot.toPlainString());
                    }

                    return builder.build();
                })
                .toList();

        return DeCoderTransactionRequest.newBuilder()
                .setRoomId(roomId.toString())
                .setRoomType(roomType.name())
                .addAllPlayerTransactions(entries)
                .build();
    }
}
