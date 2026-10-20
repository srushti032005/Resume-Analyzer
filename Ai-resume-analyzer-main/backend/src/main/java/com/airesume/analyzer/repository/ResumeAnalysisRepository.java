package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findByResumeId(Long resumeId);

    @Query("SELECT AVG(a.resumeScore) FROM ResumeAnalysis a WHERE a.resumeScore IS NOT NULL")
    Double averageResumeScore();

    @Query("SELECT AVG(a.atsScore) FROM ResumeAnalysis a WHERE a.atsScore IS NOT NULL")
    Double averageAtsScore();
}
