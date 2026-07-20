package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeExecutionResultDTO {
    private boolean success;
    private String output;
    private String aiReview;
}
