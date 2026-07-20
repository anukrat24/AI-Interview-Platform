package com.aiprep.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeExecutionRequestDTO {
    @NotBlank
    private String language; // java, python, javascript, cpp

    @NotBlank
    private String sourceCode;

    private String stdinInput = "";

    private String problemTitle;
}
