package com.websocket_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebSocketHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebSocketHubApplication.class, args);
    }
}