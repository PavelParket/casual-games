package com.bank_service.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .additionalInterceptors(((request, body, execution) -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {
                        String authHeader = attributes.getRequest().getHeader("Authorization");
                        if (authHeader != null) {
                            request.getHeaders().add("Authorization", authHeader);
                        }
                    }

                    return execution.execute(request, body);
                }))
                .build();
    }
}
