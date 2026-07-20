package com.aiprep.interview.service;

import com.aiprep.interview.dto.AdminAnalyticsDTO;
import com.aiprep.interview.dto.AdminUserViewDTO;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;

    @Override
    public List<AdminUserViewDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new AdminUserViewDTO(
                        u.getId(), u.getName(), u.getEmail(), u.getRole().name(),
                        u.getSubscriptionTier().name(), u.isBanned(),
                        interviewRepository.countByUser(u)))
                .toList();
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setBanned(false);
        userRepository.save(user);
    }

    @Override
    public AdminAnalyticsDTO getAnalytics() {
        long totalUsers = userRepository.count();
        long totalInterviews = interviewRepository.count();
        long totalPremiumUsers = userRepository.findAll().stream()
                .filter(u -> u.getSubscriptionTier() == User.SubscriptionTier.PREMIUM)
                .count();
        long totalResumes = resumeRepository.count();
        long totalCodingSubmissions = codingSubmissionRepository.count();

        double avgScore = interviewRepository.findAll().stream()
                .filter(i -> i.getOverallScore() != null)
                .mapToDouble(i -> i.getOverallScore())
                .average()
                .orElse(0.0);

        return new AdminAnalyticsDTO(totalUsers, totalInterviews, totalPremiumUsers,
                totalResumes, totalCodingSubmissions, Math.round(avgScore * 100.0) / 100.0);
    }
}
