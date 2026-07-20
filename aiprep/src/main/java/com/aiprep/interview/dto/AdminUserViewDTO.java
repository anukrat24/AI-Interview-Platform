package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserViewDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String subscriptionTier;
    private boolean banned;
    private long totalInterviews;
}
