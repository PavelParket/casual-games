package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.PlayerBet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PlayerBetFactory implements ObjectFactory<PlayerBet> {

    @Override
    public PlayerBet create(Object... objects) {
        if (objects.length != 3
                || !(objects[0] instanceof java.util.UUID guid)
                || !(objects[1] instanceof java.math.BigDecimal bet)
                || !(objects[2] instanceof java.math.BigDecimal balanceBefore)
        ) {
            throw new IllegalArgumentException("Invalid arguments: guid, bet or balance");
        }

        return create(guid, bet, balanceBefore);
    }

    private PlayerBet create(UUID guid, BigDecimal bet, BigDecimal balanceBefore) {
        return PlayerBet.builder()
                .guid(guid)
                .bet(bet)
                .balanceBefore(balanceBefore)
                .build();

    }
}
