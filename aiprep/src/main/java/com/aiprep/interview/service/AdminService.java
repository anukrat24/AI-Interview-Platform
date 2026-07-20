package com.aiprep.interview.service;

import com.aiprep.interview.dto.AdminAnalyticsDTO;
import com.aiprep.interview.dto.AdminUserViewDTO;

import java.util.List;

public interface AdminService {
    List<AdminUserViewDTO> listUsers();
    void banUser(Long userId);
    void unbanUser(Long userId);
    AdminAnalyticsDTO getAnalytics();
}
