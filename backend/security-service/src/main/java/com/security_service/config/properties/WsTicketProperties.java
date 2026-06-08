package com.security_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ws-ticket")
public record WsTicketProperties(

        Long ttlSeconds
) {
}
