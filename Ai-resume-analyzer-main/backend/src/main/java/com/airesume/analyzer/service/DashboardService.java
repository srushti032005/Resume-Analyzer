package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.response.DashboardStatsResponse;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ResumeRepository resumeRepository;
    private final SecurityUtils securityUtils;

    private static final DateTimeFormatter LABEL_FMT =
            DateTimeFormatter.ofPattern("dd MMM").withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboard() {
        Long userId = securityUtils.getCurrentUserId();
        List<Resume> resumes =
                resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long total = resumes.size();
        Integer latestScore = null;
        Integer latestAts = null;
        double sum = 0;
        int counted = 0;

        List<DashboardStatsResponse.ScorePoint> trend = new ArrayList<>();
        Map<String, Long> skillsDistribution = new LinkedHashMap<>();
        Set<String> missingSkills = new LinkedHashSet<>();

        // chronological order for trend
        List<Resume> chronological = new ArrayList<>(resumes);
        Collections.reverse(chronological);

        for (Resume r : chronological) {
            if (r.getAnalysis() != null && r.getAnalysis().getResumeScore() != null) {
                int score = r.getAnalysis().getResumeScore();
                int ats = r.getAnalysis().getAtsScore() == null
                        ? 0 : r.getAnalysis().getAtsScore();
                trend.add(new DashboardStatsResponse.ScorePoint(
                        LABEL_FMT.format(r.getCreatedAt()), score, ats));
                sum += score;
                counted++;
            }
            if (r.getExtractedSkills() != null && !r.getExtractedSkills().isBlank()) {
                for (String s : r.getExtractedSkills().split(",")) {
                    String key = s.trim();
                    if (!key.isEmpty()) {
                        skillsDistribution.merge(key, 1L, Long::sum);
                    }
                }
            }
        }

        if (!resumes.isEmpty()) {
            Resume latest = resumes.get(0);
            if (latest.getAnalysis() != null) {
                latestScore = latest.getAnalysis().getResumeScore();
                latestAts = latest.getAnalysis().getAtsScore();
                String missing = latest.getAnalysis().getMissingKeywords();
                if (missing != null && missing.contains(":")) {
                    String tail = missing.substring(missing.indexOf(":") + 1);
                    for (String m : tail.split(",")) {
                        if (!m.trim().isEmpty()) missingSkills.add(m.trim());
                    }
                }
            }
        }

        Map<String, Long> topSkills = skillsDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        Double avg = counted == 0 ? null : Math.round((sum / counted) * 10.0) / 10.0;

        return new DashboardStatsResponse(
                total, latestScore, latestAts, avg,
                trend, topSkills, new ArrayList<>(missingSkills));
    }
}
