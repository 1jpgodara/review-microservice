-- This script will run when the PostgreSQL container starts for the first time

-- Create the database (this is already handled by POSTGRES_DB in docker-compose)
-- But we can ensure proper setup

-- Enable JSON extension for storing grades as JSON
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tables (JPA will handle this, but we can define them explicitly for better control)

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    hotel_name VARCHAR(255),
    review_id VARCHAR(100) NOT NULL,
    provider_id BIGINT NOT NULL,
    rating DOUBLE PRECISION,
    review_title VARCHAR(500),
    review_comments TEXT,
    review_date TIMESTAMP,
    check_in_date VARCHAR(50),
    reviewer_country VARCHAR(100),
    reviewer_name VARCHAR(255),
    room_type VARCHAR(255),
    length_of_stay INTEGER,
    review_group_name VARCHAR(255),
    translate_source VARCHAR(10),
    translate_target VARCHAR(10),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_file VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint to prevent duplicate reviews
    UNIQUE(review_id, provider_id)
);

-- Overall ratings table
CREATE TABLE IF NOT EXISTS overall_ratings (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    provider VARCHAR(100) NOT NULL,
    overall_score DOUBLE PRECISION,
    review_count INTEGER,
    grades JSONB, -- Using JSONB for better performance
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint for hotel-provider combination
    UNIQUE(hotel_id, provider_id)
);

-- Processed files table
CREATE TABLE IF NOT EXISTS processed_files (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(500) NOT NULL UNIQUE,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    records_processed INTEGER DEFAULT 0,
    processing_duration_ms BIGINT DEFAULT 0
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_reviews_hotel_id ON reviews(hotel_id);
CREATE INDEX IF NOT EXISTS idx_reviews_platform ON reviews(platform);
CREATE INDEX IF NOT EXISTS idx_reviews_review_date ON reviews(review_date);
CREATE INDEX IF NOT EXISTS idx_reviews_provider_id ON reviews(provider_id);
CREATE INDEX IF NOT EXISTS idx_reviews_source_file ON reviews(source_file);

CREATE INDEX IF NOT EXISTS idx_overall_ratings_hotel_provider ON overall_ratings(hotel_id, provider_id);
CREATE INDEX IF NOT EXISTS idx_overall_ratings_provider ON overall_ratings(provider);

CREATE INDEX IF NOT EXISTS idx_processed_files_filename ON processed_files(filename);
CREATE INDEX IF NOT EXISTS idx_processed_files_processed_at ON processed_files(processed_at);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_reviews_updated_at 
    BEFORE UPDATE ON reviews 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_overall_ratings_updated_at 
    BEFORE UPDATE ON overall_ratings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data for testing (optional)
-- This can be commented out in production
/*
INSERT INTO reviews (
    hotel_id, platform, hotel_name, review_id, provider_id, rating,
    review_title, review_comments, review_date, reviewer_country, reviewer_name
) VALUES (
    10984, 'Agoda', 'Oscar Saigon Hotel', '948353737', 332, 6.4,
    'Perfect location and safe but hotel under renovation',
    'Hotel room is basic and very small. not much like pictures.',
    '2025-04-10 05:37:00', 'India', 'Test User'
) ON CONFLICT (review_id, provider_id) DO NOTHING;
*/