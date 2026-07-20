package com.aiprep.interview.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_plans")
@Data
public class RoadmapPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_skills", columnDefinition = "TEXT")
    private String currentSkills;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "target_company")
    private String targetCompany;

    @Column(name = "generated_plan", columnDefinition = "TEXT")
    private String generatedPlan; // markdown-formatted roadmap

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
