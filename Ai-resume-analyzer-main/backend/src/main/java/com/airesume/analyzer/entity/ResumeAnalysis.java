package com.airesume.analyzer.entity;

import com.airesume.analyzer.entity.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column
    private Integer resumeScore;

    @Column
    private Integer atsScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String missingKeywords;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String weakSections;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String grammarSuggestions;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rewriteRecommendations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String skillsGapAnalysis;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String careerRecommendations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String learningRoadmap;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiFeedbackSummary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String keywordOptimization;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobRecommendation> jobRecommendations = new ArrayList<>();
}
