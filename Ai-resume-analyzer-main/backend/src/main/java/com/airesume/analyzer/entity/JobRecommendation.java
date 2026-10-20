package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_recommendations", indexes = {
        @Index(name = "idx_job_analysis", columnList = "analysis_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRecommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ResumeAnalysis analysis;

    @Column(nullable = false, length = 120)
    private String jobTitle;

    @Column
    private Integer matchPercentage;

    @Column(length = 120)
    private String salaryEstimate;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String requiredSkillGaps;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String careerRoadmap;
}
