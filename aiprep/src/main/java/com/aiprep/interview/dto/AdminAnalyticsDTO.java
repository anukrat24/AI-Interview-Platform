package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAnalyticsDTO {
    private long totalUsers;
    private long totalInterviews;
    private long totalPremiumUsers;
    private long totalResumesAnalyzed;
    private long totalCodingSubmissions;
    private double averageInterviewScore;
}
