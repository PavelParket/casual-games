package com.bank_service.config;

import com.bank_service.domain.enums.RoomType;
import com.bank_service.factory.GameTransactionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    @Bean
    public Map<RoomType, GameTransactionFactory> transactionFactories(List<GameTransactionFactory> factories) {
        return factories.stream()
                .collect(Collectors.toMap(
                        GameTransactionFactory::getRoomType,
                        factory -> factory
                ));
    }
}
