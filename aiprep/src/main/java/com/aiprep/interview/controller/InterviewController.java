package com.aiprep.interview.controller;

import com.aiprep.interview.dto.*;
import com.aiprep.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<InterviewResponseDTO> startInterview(
            @Valid @RequestBody StartInterviewDTO request, Authentication authentication) {
        return ResponseEntity.ok(interviewService.startInterview(authentication.getName(), request));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDTO> getInterview(
            @PathVariable Long interviewId, Authentication authentication) {
        return ResponseEntity.ok(interviewService.getInterview(authentication.getName(), interviewId));
    }

    @PostMapping("/{interviewId}/answers")
    public ResponseEntity<FeedbackDTO> submitAnswer(
            @PathVariable Long interviewId, @Valid @RequestBody SubmitAnswerDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(interviewService.submitAnswer(authentication.getName(), interviewId, request));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<InterviewResponseDTO> completeInterview(
            @PathVariable Long interviewId, Authentication authentication) {
        return ResponseEntity.ok(interviewService.completeInterview(authentication.getName(), interviewId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewResponseDTO>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(interviewService.getHistory(authentication.getName()));
    }
}
