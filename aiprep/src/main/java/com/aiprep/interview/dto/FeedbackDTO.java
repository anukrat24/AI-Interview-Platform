package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDTO {
    private Double scoreOutOf10;
    private Double technicalCorrectness;
    private Double confidence;
    private Double communication;
    private Double keywordCoverage;
    private String suggestions;
    private String sampleAnswer;
}
