package com.reviewsystem.application.controller;

import com.reviewsystem.application.service.ReviewProcessingService;
import com.reviewsystem.domain.model.Review;
import com.reviewsystem.infrastructure.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewProcessingService processingService;
    private final ReviewRepository reviewRepository;

    @PostMapping("/process")
    public ResponseEntity<String> processReviews() {
        try {
            log.info("Manual review processing triggered");
            processingService.processAllFiles();
            return ResponseEntity.ok("Review processing completed successfully");
        } catch (Exception e) {
            log.error("Review processing failed", e);
            return ResponseEntity.internalServerError()
                    .body("Processing failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<Review>> getReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Review service is healthy");
    }
}
