package com.aiprep.interview.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submissions")
@Data
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "problem_title")
    private String problemTitle;

    @Column(columnDefinition = "TEXT")
    private String language; // java, python, javascript, cpp

    @Column(name = "source_code", columnDefinition = "TEXT")
    private String sourceCode;

    @Column(name = "stdin_input", columnDefinition = "TEXT")
    private String stdinInput;

    @Column(name = "execution_output", columnDefinition = "TEXT")
    private String executionOutput;

    @Column(name = "execution_success")
    private boolean executionSuccess;

    @Column(name = "ai_review", columnDefinition = "TEXT")
    private String aiReview;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
