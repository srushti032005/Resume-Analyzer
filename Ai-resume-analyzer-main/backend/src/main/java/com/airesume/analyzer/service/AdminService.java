package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.response.AdminStatsResponse;
import com.airesume.analyzer.dto.response.ResumeResponse;
import com.airesume.analyzer.dto.response.UserResponse;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.entity.enums.Role;
import com.airesume.analyzer.exception.BadRequestException;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.repository.ResumeAnalysisRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getAllResumes() {
        return resumeRepository.findAll().stream()
                .map(ResumeResponse::from).toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be deleted");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be disabled");
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getPlatformStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long totalResumes = resumeRepository.countAllResumes();
        Double avgScore = analysisRepository.averageResumeScore();
        Double avgAts = analysisRepository.averageAtsScore();

        Map<String, Long> grouped = resumeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> LocalDate.ofInstant(r.getCreatedAt(),
                                ZoneId.systemDefault()).toString(),
                        TreeMap::new,
                        Collectors.counting()));

        List<AdminStatsResponse.UploadPoint> trend = grouped.entrySet().stream()
                .map(e -> new AdminStatsResponse.UploadPoint(e.getKey(), e.getValue()))
                .toList();

        return new AdminStatsResponse(
                totalUsers, activeUsers, totalResumes,
                round(avgScore), round(avgAts), trend);
    }

    private Double round(Double v) {
        return v == null ? null : Math.round(v * 10.0) / 10.0;
    }
}
