package com.airesume.analyzer.dto.response;

import com.airesume.analyzer.entity.Resume;

import java.time.Instant;

public record ResumeResponse(
        Long id,
        String originalFilename,
        Long fileSizeBytes,
        Integer version,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        String extractedSkills,
        String extractedExperience,
        String extractedEducation,
        String extractedCertifications,
        String extractedProjects,
        String extractedKeywords,
        Instant createdAt,
        AnalysisResponse analysis
) {
    public static ResumeResponse from(Resume r) {
        return new ResumeResponse(
                r.getId(),
                r.getOriginalFilename(),
                r.getFileSizeBytes(),
                r.getVersion(),
                r.getCandidateName(),
                r.getCandidateEmail(),
                r.getCandidatePhone(),
                r.getExtractedSkills(),
                r.getExtractedExperience(),
                r.getExtractedEducation(),
                r.getExtractedCertifications(),
                r.getExtractedProjects(),
                r.getExtractedKeywords(),
                r.getCreatedAt(),
                r.getAnalysis() == null ? null : AnalysisResponse.from(r.getAnalysis())
        );
    }
}
