package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback_history", indexes = {
        @Index(name = "idx_feedback_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(nullable = false, length = 120)
    private String action;

    @Column
    private Integer resumeScore;

    @Column
    private Integer atsScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String snapshot;
}
