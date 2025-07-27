package com.reviewsystem.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.prefix:daily-reviews/}")
    private String prefix;

    public List<S3FileInfo> listJsonlFiles() {
        List<S3FileInfo> files = new ArrayList<>();
        
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response;
            do {
                response = s3Client.listObjectsV2(request);
                
                for (S3Object s3Object : response.contents()) {
                    if (s3Object.key().endsWith(".jl")) {
                        files.add(S3FileInfo.builder()
                                .key(s3Object.key())
                                .lastModified(s3Object.lastModified())
                                .size(s3Object.size())
                                .build());
                    }
                }
                
                request = request.toBuilder()
                        .continuationToken(response.nextContinuationToken())
                        .build();
                        
            } while (response.isTruncated());
            
            log.info("Found {} .jl files in S3 bucket: {}", files.size(), bucketName);
            return files;
            
        } catch (Exception e) {
            log.error("Failed to list files from S3", e);
            throw new RuntimeException("Failed to list files from S3", e);
        }
    }

    public BufferedReader downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return new BufferedReader(new InputStreamReader(response));
            
        } catch (Exception e) {
            log.error("Failed to download file: {}", key, e);
            throw new RuntimeException("Failed to download file: " + key, e);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class S3FileInfo {
        private String key;
        private Instant lastModified;
        private Long size;
    }
}
