package com.reviewsystem.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewsystem.domain.dto.ReviewJsonDto;
import com.reviewsystem.domain.model.OverallRating;
import com.reviewsystem.domain.model.ProcessedFile;
import com.reviewsystem.domain.model.Review;
import com.reviewsystem.infrastructure.repository.OverallRatingRepository;
import com.reviewsystem.infrastructure.repository.ProcessedFileRepository;
import com.reviewsystem.infrastructure.repository.ReviewRepository;
import com.reviewsystem.infrastructure.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewProcessingService {

    private final S3Service s3Service;
    private final ReviewRepository reviewRepository;
    private final OverallRatingRepository overallRatingRepository;
    private final ProcessedFileRepository processedFileRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.processing.batch-size:100}")
    private int batchSize;

    public void processAllFiles() {
        log.info("Starting review processing...");
        long startTime = System.currentTimeMillis();
        
        List<S3Service.S3FileInfo> files = s3Service.listJsonlFiles();
        List<S3Service.S3FileInfo> newFiles = filterNewFiles(files);
        
        log.info("Processing {} new files out of {} total files", newFiles.size(), files.size());
        
        AtomicInteger processedFiles = new AtomicInteger(0);
        AtomicInteger totalRecords = new AtomicInteger(0);
        
        List<CompletableFuture<ProcessingResult>> futures = newFiles.stream()
                .map(file -> processFileAsync(file)
                        .thenApply(result -> {
                            int completed = processedFiles.incrementAndGet();
                            totalRecords.addAndGet(result.getRecordsProcessed());
                            log.info("Progress: {}/{} files completed", completed, newFiles.size());
                            return result;
                        }))
                .toList();

        // Wait for all files to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Processing completed. Processed {} files, {} records in {}ms", 
                processedFiles.get(), totalRecords.get(), duration);
    }

    @Async("fileProcessingExecutor")
    public CompletableFuture<ProcessingResult> processFileAsync(S3Service.S3FileInfo fileInfo) {
        return CompletableFuture.completedFuture(processFile(fileInfo));
    }

    @Transactional
    public ProcessingResult processFile(S3Service.S3FileInfo fileInfo) {
        long startTime = System.currentTimeMillis();
        int recordsProcessed = 0;
        int lineNumber = 0;
        
        log.info("Processing file: {}", fileInfo.getKey());
        
        try (BufferedReader reader = s3Service.downloadFile(fileInfo.getKey())) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    ReviewJsonDto reviewDto = objectMapper.readValue(line, ReviewJsonDto.class);
                    
                    if (isValidReview(reviewDto)) {
                        processReviewRecord(reviewDto, fileInfo.getKey());
                        recordsProcessed++;
                    } else {
                        log.warn("Invalid review data at line {} in file {}", lineNumber, fileInfo.getKey());
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to parse line {} in file {}: {}", lineNumber, fileInfo.getKey(), e.getMessage());
                }
            }
            
            // Mark file as processed
            long duration = System.currentTimeMillis() - startTime;
            markFileAsProcessed(fileInfo.getKey(), recordsProcessed, duration);
            
            log.info("Successfully processed file {}: {} records in {}ms", 
                    fileInfo.getKey(), recordsProcessed, duration);
            
            return ProcessingResult.builder()
                    .filename(fileInfo.getKey())
                    .recordsProcessed(recordsProcessed)
                    .processingTime(duration)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to process file {}", fileInfo.getKey(), e);
            return ProcessingResult.builder()
                    .filename(fileInfo.getKey())
                    .recordsProcessed(recordsProcessed)
                    .processingTime(System.currentTimeMillis() - startTime)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    private void processReviewRecord(ReviewJsonDto reviewDto, String sourceFile) {
        // Process main review
        Review review = transformToReview(reviewDto, sourceFile);
        saveOrUpdateReview(review);
        
        // Process overall ratings
        if (reviewDto.getOverallByProviders() != null) {
            for (ReviewJsonDto.OverallProviderDto overallDto : reviewDto.getOverallByProviders()) {
                OverallRating rating = transformToOverallRating(overallDto, reviewDto.getHotelId());
                saveOrUpdateOverallRating(rating);
            }
        }
    }

    private Review transformToReview(ReviewJsonDto dto, String sourceFile) {
        ReviewJsonDto.CommentDto comment = dto.getComment();
        ReviewJsonDto.ReviewerInfoDto reviewerInfo = comment.getReviewerInfo();
        
        LocalDateTime reviewDate = null;
        if (comment.getReviewDate() != null) {
            try {
                reviewDate = LocalDateTime.parse(comment.getReviewDate(), 
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse review date: {}", comment.getReviewDate());
            }
        }
        
        return Review.builder()
                .hotelId(dto.getHotelId())
                .platform(dto.getPlatform())
                .hotelName(dto.getHotelName())
                .reviewId(String.valueOf(comment.getHotelReviewId()))
                .providerId(comment.getProviderId())
                .rating(comment.getRating())
                .reviewTitle(comment.getReviewTitle())
                .reviewComments(comment.getReviewComments())
                .reviewDate(reviewDate)
                .checkInDate(comment.getCheckInDateMonthAndYear())
                .reviewerCountry(reviewerInfo != null ? reviewerInfo.getCountryName() : null)
                .reviewerName(reviewerInfo != null ? reviewerInfo.getDisplayMemberName() : null)
                .roomType(reviewerInfo != null ? reviewerInfo.getRoomTypeName() : null)
                .lengthOfStay(reviewerInfo != null ? reviewerInfo.getLengthOfStay() : null)
                .reviewGroupName(reviewerInfo != null ? reviewerInfo.getReviewGroupName() : null)
                .translateSource(comment.getTranslateSource())
                .translateTarget(comment.getTranslateTarget())
                .sourceFile(sourceFile)
                .build();
    }

    private OverallRating transformToOverallRating(ReviewJsonDto.OverallProviderDto dto, Long hotelId) {
        return OverallRating.builder()
                .hotelId(hotelId)
                .providerId(dto.getProviderId())
                .provider(dto.getProvider())
                .overallScore(dto.getOverallScore())
                .reviewCount(dto.getReviewCount())
                .grades(dto.getGrades())
                .build();
    }

    private void saveOrUpdateReview(Review review) {
        Optional<Review> existing = reviewRepository.findByReviewIdAndProviderId(
                review.getReviewId(), review.getProviderId());
                
        if (existing.isPresent()) {
            Review existingReview = existing.get();
            existingReview.setRating(review.getRating());
            existingReview.setReviewComments(review.getReviewComments());
            existingReview.setUpdatedAt(LocalDateTime.now());
            reviewRepository.save(existingReview);
        } else {
            reviewRepository.save(review);
        }
    }

    private void saveOrUpdateOverallRating(OverallRating rating) {
        Optional<OverallRating> existing = overallRatingRepository.findByHotelIdAndProviderId(
                rating.getHotelId(), rating.getProviderId());
                
        if (existing.isPresent()) {
            OverallRating existingRating = existing.get();
            existingRating.setOverallScore(rating.getOverallScore());
            existingRating.setReviewCount(rating.getReviewCount());
            existingRating.setGrades(rating.getGrades());
            existingRating.setUpdatedAt(LocalDateTime.now());
            overallRatingRepository.save(existingRating);
        } else {
            overallRatingRepository.save(rating);
        }
    }

    private boolean isValidReview(ReviewJsonDto dto) {
        return dto.getHotelId() != null && 
               dto.getComment() != null && 
               dto.getComment().getHotelReviewId() != null &&
               dto.getComment().getProviderId() != null;
    }

    private List<S3Service.S3FileInfo> filterNewFiles(List<S3Service.S3FileInfo> files) {
        return files.stream()
                .filter(file -> !processedFileRepository.existsByFilename(file.getKey()))
                .toList();
    }

    private void markFileAsProcessed(String filename, int recordsProcessed, long duration) {
        ProcessedFile processedFile = ProcessedFile.builder()
                .filename(filename)
                .recordsProcessed(recordsProcessed)
                .processingDurationMs(duration)
                .processedAt(LocalDateTime.now())
                .build();
        processedFileRepository.save(processedFile);
    }

    @lombok.Data
    @lombok.Builder
    public static class ProcessingResult {
        private String filename;
        private int recordsProcessed;
        private long processingTime;
        private boolean success;
    }
}
