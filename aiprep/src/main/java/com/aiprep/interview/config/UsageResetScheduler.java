package com.aiprep.interview.config;

import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsageResetScheduler {

    private final UserRepository userRepository;

    // Runs every day at 00:05 server time. Also self-heals in InterviewServiceImpl
    // if a request comes in for a user whose usageResetDate is stale, so this is a
    // convenience cleanup rather than the only reset mechanism.
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void resetDailyUsage() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now();
        int resetCount = 0;
        for (User user : users) {
            if (!today.equals(user.getUsageResetDate())) {
                user.setInterviewsUsedToday(0);
                user.setUsageResetDate(today);
                resetCount++;
            }
        }
        userRepository.saveAll(users);
        log.info("Daily usage quota reset for {} users", resetCount);
    }
}
