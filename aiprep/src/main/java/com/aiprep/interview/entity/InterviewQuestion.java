package com.aiprep.interview.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "interview_questions")
@Data
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "order_index")
    private Integer orderIndex;

    private String difficulty; // EASY, MEDIUM, HARD

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewAnswer answer;
}
