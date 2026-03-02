package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class HorseRacePlayerBetFactory implements ObjectFactory<HorseRacePlayerBet> {

    @Override
    public HorseRacePlayerBet create(Object... objects) {
        if (objects.length != 5
                || !(objects[0] instanceof UUID guid)
                || !(objects[1] instanceof Integer horseIndex)
                || !(objects[2] instanceof Double odd)
                || !(objects[3] instanceof BigDecimal amount)
                || !(objects[4] instanceof BigDecimal balanceBefore)
        ) {
            throw new IllegalArgumentException("Invalid arguments: expected guid, horseIndex, odd, amount, balanceBefore");
        }

        return create(guid, horseIndex, odd, amount, balanceBefore);
    }

    private HorseRacePlayerBet create(UUID guid,
                                      Integer horseIndex,
                                      Double odd,
                                      BigDecimal amount,
                                      BigDecimal balanceBefore) {
        return HorseRacePlayerBet.builder()
                .guid(guid)
                .horseIndex(horseIndex)
                .odd(odd)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .build();
    }
}
