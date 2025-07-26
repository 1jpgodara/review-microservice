package com.reviewsystem.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_provider", columnList = "reviewId,providerId", unique = true),
    @Index(name = "idx_hotel_id", columnList = "hotelId"),
    @Index(name = "idx_platform", columnList = "platform"),
    @Index(name = "idx_review_date", columnList = "reviewDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "hotel_id")
    private Long hotelId;

    @NotNull
    @Size(max = 50)
    private String platform;

    @Size(max = 255)
    @Column(name = "hotel_name")
    private String hotelName;

    @NotNull
    @Size(max = 100)
    @Column(name = "review_id")
    private String reviewId;

    @NotNull
    @Column(name = "provider_id")
    private Long providerId;

    private Double rating;

    @Column(name = "review_title", length = 500)
    private String reviewTitle;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "check_in_date")
    private String checkInDate;

    @Column(name = "reviewer_country")
    private String reviewerCountry;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "length_of_stay")
    private Integer lengthOfStay;

    @Column(name = "review_group_name")
    private String reviewGroupName;

    @Column(name = "translate_source")
    private String translateSource;

    @Column(name = "translate_target")
    private String translateTarget;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "source_file")
    private String sourceFile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        processedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
