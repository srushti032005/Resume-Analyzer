package com.airesume.analyzer.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardStatsResponse(
        long totalResumes,
        Integer latestScore,
        Integer latestAtsScore,
        Double averageScore,
        List<ScorePoint> scoreTrend,
        Map<String, Long> skillsDistribution,
        List<String> missingSkills
) {
    public record ScorePoint(String label, Integer resumeScore, Integer atsScore) {}
}
