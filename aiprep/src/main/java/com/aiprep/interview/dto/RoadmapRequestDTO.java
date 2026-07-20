package com.aiprep.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoadmapRequestDTO {
    @NotBlank(message = "Current skills are required")
    private String currentSkills;

    @NotBlank(message = "Target role is required")
    private String targetRole;

    private String targetCompany;
}
