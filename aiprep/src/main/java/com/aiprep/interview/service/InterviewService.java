package com.aiprep.interview.service;

import com.aiprep.interview.dto.FeedbackDTO;
import com.aiprep.interview.dto.InterviewResponseDTO;
import com.aiprep.interview.dto.StartInterviewDTO;
import com.aiprep.interview.dto.SubmitAnswerDTO;

import java.util.List;

public interface InterviewService {
    InterviewResponseDTO startInterview(String userEmail, StartInterviewDTO request);
    FeedbackDTO submitAnswer(String userEmail, Long interviewId, SubmitAnswerDTO request);
    InterviewResponseDTO getInterview(String userEmail, Long interviewId);
    List<InterviewResponseDTO> getHistory(String userEmail);
    InterviewResponseDTO completeInterview(String userEmail, Long interviewId);
}
