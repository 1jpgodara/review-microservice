package com.reviewsystem.infrastructure.repository;

import com.reviewsystem.domain.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Long> {
    Optional<ProcessedFile> findByFilename(String filename);
    boolean existsByFilename(String filename);
}
