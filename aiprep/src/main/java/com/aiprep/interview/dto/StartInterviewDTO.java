package com.aiprep.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartInterviewDTO {
    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    @NotBlank(message = "Interview type is required")
    private String interviewType;

    private Integer numberOfQuestions = 5;
}
