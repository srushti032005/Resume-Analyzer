package com.airesume.analyzer.dto.response;

import com.airesume.analyzer.entity.ResumeAnalysis;

import java.util.List;

public record AnalysisResponse(
        Long id,
        String status,
        Integer resumeScore,
        Integer atsScore,
        String missingKeywords,
        String weakSections,
        String grammarSuggestions,
        String rewriteRecommendations,
        String skillsGapAnalysis,
        String careerRecommendations,
        String learningRoadmap,
        String aiFeedbackSummary,
        String keywordOptimization,
        String errorMessage,
        List<JobRecommendationResponse> jobRecommendations
) {
    public static AnalysisResponse from(ResumeAnalysis a) {
        return new AnalysisResponse(
                a.getId(),
                a.getStatus().name(),
                a.getResumeScore(),
                a.getAtsScore(),
                a.getMissingKeywords(),
                a.getWeakSections(),
                a.getGrammarSuggestions(),
                a.getRewriteRecommendations(),
                a.getSkillsGapAnalysis(),
                a.getCareerRecommendations(),
                a.getLearningRoadmap(),
                a.getAiFeedbackSummary(),
                a.getKeywordOptimization(),
                a.getErrorMessage(),
                a.getJobRecommendations() == null ? List.of() :
                        a.getJobRecommendations().stream()
                                .map(JobRecommendationResponse::from).toList()
        );
    }
}
