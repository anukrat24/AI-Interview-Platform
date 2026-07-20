package com.aiprep.interview.service;

import com.aiprep.interview.ai.AiInterviewService;
import com.aiprep.interview.dto.*;
import com.aiprep.interview.entity.*;
import com.aiprep.interview.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewAnswerRepository answerRepository;
    private final AiFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AiInterviewService aiInterviewService;

    @Value("${app.free-interviews-per-day:3}")
    private int freeInterviewsPerDay;

    @Override
    @Transactional
    public InterviewResponseDTO startInterview(String userEmail, StartInterviewDTO request) {
        User user = userService.getUserByEmail(userEmail);
        enforceAndConsumeDailyQuota(user);

        Interview interview = new Interview();
        interview.setUser(user);
        interview.setRole(request.getRole());
        interview.setExperienceLevel(request.getExperienceLevel());
        interview.setInterviewType(request.getInterviewType());
        interview.setStatus("IN_PROGRESS");
        interviewRepository.save(interview);

        List<AiInterviewService.GeneratedQuestion> generated = aiInterviewService.generateQuestions(
                request.getRole(), request.getExperienceLevel(), request.getInterviewType(),
                request.getNumberOfQuestions() == null ? 5 : request.getNumberOfQuestions()
        );

        int order = 1;
        for (AiInterviewService.GeneratedQuestion gq : generated) {
            InterviewQuestion question = new InterviewQuestion();
            question.setInterview(interview);
            question.setQuestionText(gq.questionText());
            question.setDifficulty(gq.difficulty());
            question.setOrderIndex(order++);
            interview.getQuestions().add(question);
        }
        interviewRepository.save(interview);

        return toResponseDto(interview);
    }

    /** Free-tier users get a limited number of new interviews per day; Premium is unlimited. */
    private void enforceAndConsumeDailyQuota(User user) {
        if (user.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && (user.getSubscriptionExpiry() == null || user.getSubscriptionExpiry().isAfter(LocalDateTime.now()))) {
            return; // unlimited for active premium users
        }

        LocalDate today = LocalDate.now();
        if (!today.equals(user.getUsageResetDate())) {
            user.setInterviewsUsedToday(0);
            user.setUsageResetDate(today);
        }

        if (user.getInterviewsUsedToday() >= freeInterviewsPerDay) {
            throw new IllegalStateException(
                    "You've used your " + freeInterviewsPerDay + " free interviews for today. " +
                    "Upgrade to Premium for unlimited interviews, or come back tomorrow.");
        }

        user.setInterviewsUsedToday(user.getInterviewsUsedToday() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public FeedbackDTO submitAnswer(String userEmail, Long interviewId, SubmitAnswerDTO request) {
        Interview interview = getOwnedInterview(userEmail, interviewId);

        InterviewQuestion question = interview.getQuestions().stream()
                .filter(q -> q.getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Question not found in this interview"));

        InterviewAnswer answer = new InterviewAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(request.getAnswerText());
        answer.setWasVoiceInput(request.isVoiceInput());
        answerRepository.save(answer);

        FeedbackDTO feedbackDto = aiInterviewService.evaluateAnswer(
                question.getQuestionText(), request.getAnswerText(), interview.getRole());

        AiFeedback feedback = new AiFeedback();
        feedback.setAnswer(answer);
        feedback.setScoreOutOf10(feedbackDto.getScoreOutOf10());
        feedback.setTechnicalCorrectness(feedbackDto.getTechnicalCorrectness());
        feedback.setConfidence(feedbackDto.getConfidence());
        feedback.setCommunication(feedbackDto.getCommunication());
        feedback.setKeywordCoverage(feedbackDto.getKeywordCoverage());
        feedback.setSuggestions(feedbackDto.getSuggestions());
        feedback.setSampleAnswer(feedbackDto.getSampleAnswer());
        feedbackRepository.save(feedback);

        return feedbackDto;
    }

    // after
    @Override
    @Transactional(readOnly = true)
    public InterviewResponseDTO getInterview(String userEmail, Long interviewId) {
        return toResponseDto(getOwnedInterview(userEmail, interviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDTO> getHistory(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        return interviewRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toResponseDto).toList();
    }

    @Override
    @Transactional
    public InterviewResponseDTO completeInterview(String userEmail, Long interviewId) {
        Interview interview = getOwnedInterview(userEmail, interviewId);

        double average = interview.getQuestions().stream()
                .map(InterviewQuestion::getAnswer)
                .filter(a -> a != null && a.getFeedback() != null)
                .mapToDouble(a -> a.getFeedback().getScoreOutOf10())
                .average()
                .orElse(0.0);

        interview.setOverallScore(Math.round(average * 100.0) / 100.0);
        interview.setStatus("COMPLETED");
        interview.setCompletedAt(LocalDateTime.now());
        interviewRepository.save(interview);

        return toResponseDto(interview);
    }

    private Interview getOwnedInterview(String userEmail, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

        if (!interview.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You do not have access to this interview");
        }
        return interview;
    }

    private InterviewResponseDTO toResponseDto(Interview interview) {
        List<InterviewResponseDTO.QuestionDTO> questionDtos = interview.getQuestions().stream()
                .map(q -> new InterviewResponseDTO.QuestionDTO(
                        q.getId(), q.getQuestionText(), q.getOrderIndex(), q.getDifficulty()))
                .toList();

        return new InterviewResponseDTO(
                interview.getId(),
                interview.getRole(),
                interview.getExperienceLevel(),
                interview.getInterviewType(),
                interview.getStatus(),
                interview.getOverallScore(),
                questionDtos
        );
    }
}
