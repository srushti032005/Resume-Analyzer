package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skills", indexes = {
        @Index(name = "idx_skill_resume", columnList = "resume_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 40)
    private String category;

    /**
     * Whether the skill was detected in the resume (true) or
     * flagged as a recommended/missing skill (false).
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean present = true;

    @Column
    private Integer relevanceScore;
}
