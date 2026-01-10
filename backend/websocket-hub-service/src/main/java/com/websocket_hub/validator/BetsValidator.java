package com.websocket_hub.validator;

import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BetsValidator {

    public boolean validateBets(List<PlayerBet> playerBets) {
        if (playerBets == null || playerBets.isEmpty()) {
            log.warn("Player bets list is null or empty");
            return false;
        }


    }
}
