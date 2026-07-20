package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapResponseDTO {
    private Long roadmapId;
    private String generatedPlan;
}
