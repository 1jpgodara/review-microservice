package com.reviewsystem.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewsystem.domain.dto.ReviewJsonDto;
import com.reviewsystem.domain.model.Review;
import com.reviewsystem.infrastructure.repository.OverallRatingRepository;
import com.reviewsystem.infrastructure.repository.ProcessedFileRepository;
import com.reviewsystem.infrastructure.repository.ReviewRepository;
import com.reviewsystem.infrastructure.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewProcessingServiceTest {

    @Mock
    private S3Service s3Service;
    
    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private OverallRatingRepository overallRatingRepository;
    
    @Mock
    private ProcessedFileRepository processedFileRepository;

    private ReviewProcessingService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ReviewProcessingService(
                s3Service, reviewRepository, overallRatingRepository, 
                processedFileRepository, objectMapper);
        ReflectionTestUtils.setField(service, "batchSize", 100);
    }

    @Test
    void testProcessFile_ValidData() {
        // Given
        String jsonLine = """
            {
                "hotelId": 10984,
                "platform": "Agoda",
                "hotelName": "Oscar Saigon Hotel",
                "comment": {
                    "hotelReviewId": 948353737,
                    "providerId": 332,
                    "rating": 6.4,
                    "reviewTitle": "Perfect location",
                    "reviewComments": "Hotel room is basic",
                    "reviewDate": "2025-04-10T05:37:00+07:00",
                    "reviewerInfo": {
                        "countryName": "India",
                        "displayMemberName": "John Doe"
                    }
                },
                "overallByProviders": []
            }
            """;

        S3Service.S3FileInfo fileInfo = S3Service.S3FileInfo.builder()
                .key("test-file.jl")
                .lastModified(Instant.now())
                .size(1000L)
                .build();

        BufferedReader reader = new BufferedReader(new StringReader(jsonLine));
        
        when(s3Service.downloadFile(anyString())).thenReturn(reader);
        when(reviewRepository.findByReviewIdAndProviderId(anyString(), any())).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());

        // When
        ReviewProcessingService.ProcessingResult result = service.processFile(fileInfo);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordsProcessed());
        verify(reviewRepository).save(any(Review.class));
        verify(processedFileRepository).save(any());
    }

    @Test
    void testProcessFile_InvalidJson() {
        // Given
        String invalidJsonLine = "{ invalid json }";
        
        S3Service.S3FileInfo fileInfo = S3Service.S3FileInfo.builder()
                .key("test-file.jl")
                .lastModified(Instant.now())
                .size(1000L)
                .build();

        BufferedReader reader = new BufferedReader(new StringReader(invalidJsonLine));
        when(s3Service.downloadFile(anyString())).thenReturn(reader);

        // When
        ReviewProcessingService.ProcessingResult result = service.processFile(fileInfo);

        // Then
        assertTrue(result.isSuccess()); // File processing succeeds even with invalid lines
        assertEquals(0, result.getRecordsProcessed());
        verify(reviewRepository, never()).save(any());
    }
}
