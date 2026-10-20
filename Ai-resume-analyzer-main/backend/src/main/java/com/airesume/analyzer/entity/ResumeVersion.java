package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_versions", indexes = {
        @Index(name = "idx_version_resume", columnList = "resume_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 512)
    private String storedPath;

    @Column
    private Integer resumeScore;

    @Column
    private Integer atsScore;
}
