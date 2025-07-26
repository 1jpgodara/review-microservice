package com.reviewsystem.infrastructure.repository;

import com.reviewsystem.domain.model.OverallRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OverallRatingRepository extends JpaRepository<OverallRating, Long> {
    Optional<OverallRating> findByHotelIdAndProviderId(Long hotelId, Long providerId);
}
