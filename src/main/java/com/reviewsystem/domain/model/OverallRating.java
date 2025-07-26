package com.reviewsystem.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "overall_ratings", indexes = {
    @Index(name = "idx_hotel_provider", columnList = "hotelId,providerId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "hotel_id")
    private Long hotelId;

    @NotNull
    @Column(name = "provider_id")
    private Long providerId;

    @NotNull
    private String provider;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "review_count")
    private Integer reviewCount;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Double> grades;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
