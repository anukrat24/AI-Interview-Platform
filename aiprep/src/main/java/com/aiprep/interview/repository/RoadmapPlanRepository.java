package com.aiprep.interview.repository;

import com.aiprep.interview.entity.RoadmapPlan;
import com.aiprep.interview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapPlanRepository extends JpaRepository<RoadmapPlan, Long> {
    List<RoadmapPlan> findByUserOrderByCreatedAtDesc(User user);
}
