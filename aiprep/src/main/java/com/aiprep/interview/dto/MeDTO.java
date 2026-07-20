package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeDTO {
    private String name;
    private String email;
    private String role;
    private String subscriptionTier;
    private int interviewsUsedToday;
    private int freeInterviewsPerDay;
}
