package com.aiprep.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitAnswerDTO {
    @NotNull(message = "questionId is required")
    private Long questionId;

    @NotBlank(message = "answerText is required")
    private String answerText;

    private boolean voiceInput = false;
}
