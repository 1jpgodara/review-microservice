package com.reviewsystem.application.scheduler;

import com.reviewsystem.application.service.ReviewProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class ReviewProcessingScheduler {

    private final ReviewProcessingService processingService;

    @Scheduled(cron = "${app.scheduling.cron:0 0 2 * * ?}") // Daily at 2 AM
    public void scheduleReviewProcessing() {
        log.info("Scheduled review processing started");
        try {
            processingService.processAllFiles();
            log.info("Scheduled review processing completed successfully");
        } catch (Exception e) {
            log.error("Scheduled review processing failed", e);
        }
    }
}
