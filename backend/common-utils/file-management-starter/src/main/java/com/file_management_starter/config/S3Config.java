package com.file_management_starter.config;

import com.file_management_starter.service.S3Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(value = "aws.s3.enabled-s3-config", matchIfMissing = true)
public class S3Config {

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(S3ClientProperties properties) {
        var credentialsProvider = StringUtils.isNotBlank(properties.getAccessKeyId())
                && StringUtils.isNotBlank(properties.getSecretAccessKey())
                ? StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey()))
                : DefaultCredentialsProvider.builder().build();

        var s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Service s3Service(S3Client s3Client) {
        return new S3Service(s3Client);
    }
}
