package com.aiprep.interview.service;

import com.aiprep.interview.dto.CodeExecutionRequestDTO;
import com.aiprep.interview.dto.CodeExecutionResultDTO;

import java.util.List;

public interface CodingService {
    CodeExecutionResultDTO runCode(String userEmail, CodeExecutionRequestDTO request);
    List<CodeExecutionResultDTO> getHistory(String userEmail);
}
