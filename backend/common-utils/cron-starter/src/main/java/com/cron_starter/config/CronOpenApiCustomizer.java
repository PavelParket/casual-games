package com.cron_starter.config;

import com.cron_starter.service.CronService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.cron_starter.config.Constants.CRON_ADMIN_PATH_PREFIX;
import static com.cron_starter.config.Constants.CRON_CODE_PARAM_NAME;
import static com.cron_starter.config.Constants.SPRINGDOC_CRON_TAG;

@Configuration
@ConditionalOnClass(GlobalOpenApiCustomizer.class)
public class CronOpenApiCustomizer {

    @Bean
    public GlobalOpenApiCustomizer cronTagCustomizer(Collection<CronService> cronServices) {
        return openApi -> {
            reorderCronTagFirst(openApi);
            applyCronCodeEnum(openApi, cronServices);
        };
    }

    private void reorderCronTagFirst(OpenAPI openApi) {
        List<Tag> tags = openApi.getTags();

        if (tags == null) {
            return;
        }

        tags.stream()
                .filter(tag -> SPRINGDOC_CRON_TAG.equals(tag.getName()))
                .findFirst()
                .ifPresent(tag -> {
                    tags.remove(tag);
                    tags.addFirst(tag);
                });
    }

    private void applyCronCodeEnum(OpenAPI openApi, Collection<CronService> cronServices) {
        Paths paths = openApi.getPaths();

        if (paths == null || cronServices.isEmpty()) {
            return;
        }

        List<String> codes = cronServices.stream()
                .map(CronService::getCode)
                .sorted()
                .toList();

        paths.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(CRON_ADMIN_PATH_PREFIX))
                .flatMap(entry -> entry.getValue().readOperations().stream())
                .flatMap(operation -> operation.getParameters() == null
                        ? Stream.empty()
                        : operation.getParameters().stream())
                .filter(parameter -> CRON_CODE_PARAM_NAME.equals(parameter.getName()) && parameter.getSchema() != null)
                .forEach(parameter -> parameter.getSchema()
                        .setEnum(new ArrayList<>(codes))
                );
    }
}

