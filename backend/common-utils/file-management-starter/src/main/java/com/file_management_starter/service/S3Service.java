package com.file_management_starter.service;

import com.file_management_starter.exception.S3OperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.Map;

import static com.file_management_starter.config.ResourceMessageConstants.DELETE_OBJECTS_FROM_BUCKET_ERROR;
import static com.file_management_starter.config.ResourceMessageConstants.DELETE_OBJECT_ERROR;
import static com.file_management_starter.config.ResourceMessageConstants.DOWNLOAD_OBJECT_ERROR;
import static com.file_management_starter.config.ResourceMessageConstants.UPLOAD_OBJECT_ERROR;

@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    public void upload(String bucket, String key, byte[] content, String contentType, String cacheControl, Map<String, String> userMetadata) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .cacheControl(cacheControl)
                    .metadata(userMetadata != null ? userMetadata : Map.of())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));

            log.debug("Uploaded S3 object: bucket={}, key={}", bucket, key);
        } catch (SdkException e) {
            log.error("Failed to upload S3 object: bucket={}, key={}", bucket, key, e);
            throw new S3OperationException(String.format(UPLOAD_OBJECT_ERROR, key), e);
        }
    }

    public void delete(String bucket, String key) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

            log.debug("Deleted S3 object: bucket={}, key={}", bucket, key);
        } catch (SdkException e) {
            log.error("Failed to delete S3 object: bucket={}, key={}", bucket, key, e);
            throw new S3OperationException(String.format(DELETE_OBJECT_ERROR, key), e);
        }
    }

    public void deleteAll(String bucket, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            var objects = keys.stream()
                    .map(k -> ObjectIdentifier.builder().key(k).build())
                    .toList();

            var request = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objects).build())
                    .build();

            s3Client.deleteObjects(request);

            log.debug("Deleted {} S3 objects from bucket={}", keys.size(), bucket);
        } catch (SdkException e) {
            log.error("Failed to delete S3 objects: bucket={}, keys={}", bucket, keys, e);
            throw new S3OperationException(String.format(DELETE_OBJECTS_FROM_BUCKET_ERROR, bucket), e);
        }
    }

    public byte[] download(String bucket, String key) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            log.debug("Downloaded S3 object: bucket={}, key={}", bucket, key);
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            log.warn("S3 object not found: bucket={}, key={}", bucket, key);
            throw e;
        } catch (SdkException e) {
            log.error("Failed to download S3 object: bucket={}, key={}", bucket, key, e);
            throw new S3OperationException(String.format(DOWNLOAD_OBJECT_ERROR, key), e);
        }
    }
}
