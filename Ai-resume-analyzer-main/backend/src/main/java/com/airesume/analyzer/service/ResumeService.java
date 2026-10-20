package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.response.ResumeResponse;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.entity.enums.AnalysisStatus;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.repository.*;
import com.airesume.analyzer.util.PdfTextExtractor;
import com.airesume.analyzer.util.ResumeParser;
import com.airesume.analyzer.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final SkillRepository skillRepository;
    private final FeedbackHistoryRepository feedbackHistoryRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractor pdfTextExtractor;
    private final ResumeParser resumeParser;
    private final AnalysisService analysisService;
    private final SecurityUtils securityUtils;

    @Transactional
    public ResumeResponse uploadAndAnalyze(MultipartFile file) {
        User user = securityUtils.getCurrentUser();
        fileStorageService.validate(file);

        long existingCount = resumeRepository.countByUserId(user.getId());
        String storedPath = fileStorageService.store(file, user.getId());

        byte[] bytes = fileStorageService.read(storedPath);
        String rawText = pdfTextExtractor.extractText(bytes);
        ResumeParser.ParsedResume parsed = resumeParser.parse(rawText);

        Resume resume = Resume.builder()
                .user(user)
                .originalFilename(file.getOriginalFilename())
                .storedPath(storedPath)
                .fileSizeBytes(file.getSize())
                .version((int) existingCount + 1)
                .candidateName(parsed.getName())
                .candidateEmail(parsed.getEmail())
                .candidatePhone(parsed.getPhone())
                .rawText(rawText)
                .extractedSkills(joinList(parsed.getSkills()))
                .extractedExperience(parsed.getExperience())
                .extractedEducation(parsed.getEducation())
                .extractedCertifications(parsed.getCertifications())
                .extractedProjects(parsed.getProjects())
                .extractedKeywords(joinList(parsed.getKeywords()))
                .build();

        resume = resumeRepository.save(resume);

        // Skill rows (normalized)
        if (parsed.getSkills() != null) {
            final Resume saved = resume;
            List<Skill> skillEntities = parsed.getSkills().stream()
                    .map(s -> Skill.builder()
                            .resume(saved)
                            .name(s)
                            .category("technical")
                            .present(true)
                            .relevanceScore(80)
                            .build())
                    .collect(Collectors.toList());
            skillRepository.saveAll(skillEntities);
        }

        // Version row
        ResumeVersion version = ResumeVersion.builder()
                .resume(resume)
                .versionNumber(resume.getVersion())
                .originalFilename(resume.getOriginalFilename())
                .storedPath(storedPath)
                .build();
        resumeVersionRepository.save(version);

        // Create pending analysis and dispatch async AI work
        ResumeAnalysis analysis = analysisService.createPendingAnalysis(resume);
        resume.setAnalysis(analysis);

        feedbackHistoryRepository.save(FeedbackHistory.builder()
                .user(user)
                .resumeId(resume.getId())
                .action("RESUME_UPLOADED")
                .build());

        analysisService.runAnalysisAsync(resume.getId());

        return ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getMyResumes() {
        Long userId = securityUtils.getCurrentUserId();
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(ResumeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        return ResumeResponse.from(resume);
    }

    @Transactional
    public void deleteResume(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        fileStorageService.delete(resume.getStoredPath());
        resumeRepository.delete(resume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse reanalyze(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (resume.getAnalysis() != null) {
            resume.getAnalysis().setStatus(AnalysisStatus.PENDING);
        }
        analysisService.runAnalysisAsync(resume.getId());
        return ResumeResponse.from(resume);
    }

    private String joinList(List<String> values) {
        return values == null ? null : String.join(", ", values);
    }
}
