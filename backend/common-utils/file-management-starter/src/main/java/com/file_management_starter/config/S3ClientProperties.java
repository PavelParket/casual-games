package com.file_management_starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aws.s3")
public class S3ClientProperties {

    private boolean enabledS3Config;

    private String endpoint;

    private String region;

    private String accessKeyId;

    private String secretAccessKey;
}
