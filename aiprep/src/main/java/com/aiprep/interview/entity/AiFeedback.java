package com.aiprep.interview.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ai_feedback")
@Data
public class AiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false, unique = true)
    private InterviewAnswer answer;

    @Column(name = "score_out_of_10")
    private Double scoreOutOf10;
    @Column(name = "technical_correctness")
    private Double technicalCorrectness;
    private Double confidence;
    private Double communication;
    @Column(name = "keyword_coverage")
    private Double keywordCoverage;

    @Column(columnDefinition = "TEXT")
    private String suggestions;
    @Column(name = "sample_answer", columnDefinition = "TEXT")
    private String sampleAnswer;
}
