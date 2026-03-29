package com.grpc_utils.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "grpc.client-custom")
public class GrpcClientProperties {

    private Long timeoutSeconds = 30L;

    private GrpcRetryProperties retry = new GrpcRetryProperties();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class GrpcRetryProperties {

        private int maxAttempts = 3;

        private Long delayMilliseconds = 2000L;
    }
}
