package com.common_utils.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI(
            @Value("${spring.application.name:application}") String appName,
            @Value("${app.swagger.server-url:}") String serverUrl
    ) {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .version("1.0.0"));

        if (StringUtils.hasText(serverUrl)) {
            api.addServersItem(new Server().url(serverUrl));
        }

        return api;
    }
}
