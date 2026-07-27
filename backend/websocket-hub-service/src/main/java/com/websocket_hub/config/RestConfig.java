package com.websocket_hub.config;

import com.websocket_hub.handler.RestErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestConfig {

    private final RestErrorHandler restErrorHandler;

    @Value("${app.game-service.service-token}")
    private String gameServiceToken;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .errorHandler(restErrorHandler)
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(gameServiceToken);
                    return execution.execute(request, body);
                })
                .build();
    }
}
