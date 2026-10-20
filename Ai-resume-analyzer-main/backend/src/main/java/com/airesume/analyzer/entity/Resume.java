package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resume_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 512)
    private String storedPath;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    // ---- Parsed fields ----
    @Column(length = 160)
    private String candidateName;

    @Column(length = 180)
    private String candidateEmail;

    @Column(length = 60)
    private String candidatePhone;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedSkills;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedExperience;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedEducation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedCertifications;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedProjects;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedKeywords;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResumeAnalysis analysis;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumeVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();
}
