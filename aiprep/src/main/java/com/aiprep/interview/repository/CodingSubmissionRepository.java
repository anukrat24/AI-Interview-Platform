package com.aiprep.interview.repository;

import com.aiprep.interview.entity.CodingSubmission;
import com.aiprep.interview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByUserOrderByCreatedAtDesc(User user);
}
