package com.aiprep.interview.service;

import com.aiprep.interview.ai.AiCodeReviewService;
import com.aiprep.interview.ai.PistonCodeExecutionClient;
import com.aiprep.interview.dto.CodeExecutionRequestDTO;
import com.aiprep.interview.dto.CodeExecutionResultDTO;
import com.aiprep.interview.entity.CodingSubmission;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.CodingSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodingServiceImpl implements CodingService {

    private final CodingSubmissionRepository submissionRepository;
    private final UserService userService;
    private final PistonCodeExecutionClient pistonCodeExecutionClient;
    private final AiCodeReviewService aiCodeReviewService;

    @Override
    @Transactional
    public CodeExecutionResultDTO runCode(String userEmail, CodeExecutionRequestDTO request) {
        User user = userService.getUserByEmail(userEmail);

        PistonCodeExecutionClient.ExecutionResult execResult = pistonCodeExecutionClient.execute(
                request.getLanguage(), request.getSourceCode(), request.getStdinInput());

        String aiReview = aiCodeReviewService.reviewCode(
                request.getLanguage(), request.getSourceCode(), execResult.output(), request.getProblemTitle());

        CodingSubmission submission = new CodingSubmission();
        submission.setUser(user);
        submission.setProblemTitle(request.getProblemTitle());
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(request.getSourceCode());
        submission.setStdinInput(request.getStdinInput());
        submission.setExecutionOutput(execResult.output());
        submission.setExecutionSuccess(execResult.success());
        submission.setAiReview(aiReview);
        submissionRepository.save(submission);

        return new CodeExecutionResultDTO(execResult.success(), execResult.output(), aiReview);
    }

    @Override
    public List<CodeExecutionResultDTO> getHistory(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        return submissionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(s -> new CodeExecutionResultDTO(s.isExecutionSuccess(), s.getExecutionOutput(), s.getAiReview()))
                .toList();
    }
}
