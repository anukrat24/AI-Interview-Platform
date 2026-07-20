package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewResponseDTO {
    private Long interviewId;
    private String role;
    private String experienceLevel;
    private String interviewType;
    private String status;
    private Double overallScore;
    private List<QuestionDTO> questions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionDTO {
        private Long questionId;
        private String questionText;
        private Integer orderIndex;
        private String difficulty;
    }
}
