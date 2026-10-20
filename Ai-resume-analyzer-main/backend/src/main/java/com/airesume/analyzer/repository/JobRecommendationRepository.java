package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.JobRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    List<JobRecommendation> findByAnalysisId(Long analysisId);
}
