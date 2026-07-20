package com.aiprep.interview.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "ats_score")
    private Double atsScore;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords; // comma-separated

    @Column(name = "weak_points", columnDefinition = "TEXT")
    private String weakPoints;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
