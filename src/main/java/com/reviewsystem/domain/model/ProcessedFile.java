package com.reviewsystem.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String filename;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "processing_duration_ms")
    private Long processingDurationMs;

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
