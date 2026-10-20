package com.airesume.analyzer.dto.response;

import java.util.List;

public record AdminStatsResponse(
        long totalUsers,
        long activeUsers,
        long totalResumes,
        Double averageResumeScore,
        Double averageAtsScore,
        List<UploadPoint> uploadsTrend
) {
    public record UploadPoint(String date, long count) {}
}
