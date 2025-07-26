package com.reviewsystem.infrastructure.repository;

import com.reviewsystem.domain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByReviewIdAndProviderId(String reviewId, Long providerId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sourceFile = :filename")
    long countBySourceFile(@Param("filename") String filename);
    
    @Modifying
    @Query("DELETE FROM Review r WHERE r.sourceFile = :filename")
    void deleteBySourceFile(@Param("filename") String filename);
}
