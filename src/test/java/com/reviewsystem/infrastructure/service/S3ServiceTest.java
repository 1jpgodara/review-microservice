package com.reviewsystem.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "prefix", "test-prefix/");
    }

    @Test
    void testListJsonlFiles() {
        // Given
        S3Object s3Object1 = S3Object.builder()
                .key("test-prefix/file1.jl")
                .lastModified(Instant.now())
                .size(1000L)
                .build();
                
        S3Object s3Object2 = S3Object.builder()
                .key("test-prefix/file2.txt")
                .lastModified(Instant.now())
                .size(500L)
                .build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(s3Object1, s3Object2)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // When
        List<S3Service.S3FileInfo> files = s3Service.listJsonlFiles();

        // Then
        assertEquals(1, files.size());
        assertEquals("test-prefix/file1.jl", files.get(0).getKey());
    }
}
