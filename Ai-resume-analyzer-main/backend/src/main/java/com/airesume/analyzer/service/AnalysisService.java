package com.airesume.analyzer.service;

import com.airesume.analyzer.entity.JobRecommendation;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.ResumeAnalysis;
import com.airesume.analyzer.entity.enums.AnalysisStatus;
import com.airesume.analyzer.repository.JobRecommendationRepository;
import com.airesume.analyzer.repository.ResumeAnalysisRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final JobRecommendationRepository jobRecommendationRepository;
    private final OpenAiService openAiService;

    @Transactional
    public ResumeAnalysis createPendingAnalysis(Resume resume) {
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .status(AnalysisStatus.PENDING)
                .build();
        return analysisRepository.save(analysis);
    }

    @Async("analysisExecutor")
    public void runAnalysisAsync(Long resumeId) {
        try {
            process(resumeId);
        } catch (Exception e) {
            log.error("Analysis failed for resume {}: {}", resumeId, e.getMessage());
            markFailed(resumeId, e.getMessage());
        }
    }

    @Transactional
    public void process(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalStateException("Resume vanished: " + resumeId));

        ResumeAnalysis analysis = analysisRepository.findByResumeId(resumeId)
                .orElseGet(() -> createPendingAnalysis(resume));
        analysis.setStatus(AnalysisStatus.PROCESSING);
        analysisRepository.save(analysis);

        JsonNode result = openAiService.analyzeResume(resume.getRawText());

        analysis.setResumeScore(result.path("resumeScore").asInt(0));
        analysis.setAtsScore(result.path("atsScore").asInt(0));
        analysis.setMissingKeywords(result.path("missingKeywords").asText(""));
        analysis.setWeakSections(result.path("weakSections").asText(""));
        analysis.setGrammarSuggestions(result.path("grammarSuggestions").asText(""));
        analysis.setRewriteRecommendations(result.path("rewriteRecommendations").asText(""));
        analysis.setSkillsGapAnalysis(result.path("skillsGapAnalysis").asText(""));
        analysis.setCareerRecommendations(result.path("careerRecommendations").asText(""));
        analysis.setLearningRoadmap(result.path("learningRoadmap").asText(""));
        analysis.setAiFeedbackSummary(result.path("aiFeedbackSummary").asText(""));
        analysis.setKeywordOptimization(result.path("keywordOptimization").asText(""));
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setErrorMessage(null);

        jobRecommendationRepository.deleteAll(
                jobRecommendationRepository.findByAnalysisId(analysis.getId()));

        JsonNode jobs = result.path("jobRecommendations");
        if (jobs.isArray()) {
            for (JsonNode job : jobs) {
                JobRecommendation rec = JobRecommendation.builder()
                        .analysis(analysis)
                        .jobTitle(job.path("jobTitle").asText("Role"))
                        .matchPercentage(job.path("matchPercentage").asInt(0))
                        .salaryEstimate(job.path("salaryEstimate").asText(""))
                        .requiredSkillGaps(job.path("requiredSkillGaps").asText(""))
                        .careerRoadmap(job.path("careerRoadmap").asText(""))
                        .build();
                jobRecommendationRepository.save(rec);
            }
        }

        analysisRepository.save(analysis);
        log.info("Analysis completed for resume {} (score={}, ats={})",
                resumeId, analysis.getResumeScore(), analysis.getAtsScore());
    }

    @Transactional
    public void markFailed(Long resumeId, String message) {
        analysisRepository.findByResumeId(resumeId).ifPresent(a -> {
            a.setStatus(AnalysisStatus.FAILED);
            a.setErrorMessage(message);
            analysisRepository.save(a);
        });
    }
}
