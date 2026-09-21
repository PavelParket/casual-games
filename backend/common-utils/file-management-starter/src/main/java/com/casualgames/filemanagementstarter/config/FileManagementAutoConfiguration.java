package com.casualgames.filemanagementstarter.config;

import com.casualgames.filemanagementstarter.exception.S3ExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(S3ClientProperties.class)
@Import({S3Config.class, S3ExceptionHandler.class})
public class FileManagementAutoConfiguration {
}
