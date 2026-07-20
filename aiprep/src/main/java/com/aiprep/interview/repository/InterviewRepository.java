package com.aiprep.interview.repository;

import com.aiprep.interview.entity.Interview;
import com.aiprep.interview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserOrderByCreatedAtDesc(User user);
    long countByUser(User user);
}
