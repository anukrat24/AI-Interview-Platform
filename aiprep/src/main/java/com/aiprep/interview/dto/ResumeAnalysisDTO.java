package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeAnalysisDTO {
    private Long resumeId;
    private Double atsScore;
    private List<String> missingKeywords;
    private String weakPoints;
    private String improvementSuggestions;
}
