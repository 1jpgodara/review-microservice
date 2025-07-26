package com.reviewsystem.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReviewJsonDto {
    @JsonProperty("hotelId")
    private Long hotelId;
    
    private String platform;
    
    @JsonProperty("hotelName")
    private String hotelName;
    
    private CommentDto comment;
    
    @JsonProperty("overallByProviders")
    private List<OverallProviderDto> overallByProviders;

    @Data
    public static class CommentDto {
        @JsonProperty("hotelReviewId")
        private Long hotelReviewId;
        
        @JsonProperty("providerId")
        private Long providerId;
        
        private Double rating;
        
        @JsonProperty("checkInDateMonthAndYear")
        private String checkInDateMonthAndYear;
        
        @JsonProperty("reviewTitle")
        private String reviewTitle;
        
        @JsonProperty("reviewComments")
        private String reviewComments;
        
        @JsonProperty("reviewDate")
        private String reviewDate;
        
        @JsonProperty("translateSource")
        private String translateSource;
        
        @JsonProperty("translateTarget")
        private String translateTarget;
        
        @JsonProperty("reviewerInfo")
        private ReviewerInfoDto reviewerInfo;
    }

    @Data
    public static class ReviewerInfoDto {
        @JsonProperty("countryName")
        private String countryName;
        
        @JsonProperty("displayMemberName")
        private String displayMemberName;
        
        @JsonProperty("reviewGroupName")
        private String reviewGroupName;
        
        @JsonProperty("roomTypeName")
        private String roomTypeName;
        
        @JsonProperty("lengthOfStay")
        private Integer lengthOfStay;
    }

    @Data
    public static class OverallProviderDto {
        @JsonProperty("providerId")
        private Long providerId;
        
        private String provider;
        
        @JsonProperty("overallScore")
        private Double overallScore;
        
        @JsonProperty("reviewCount")
        private Integer reviewCount;
        
        private java.util.Map<String, Double> grades;
    }
}
