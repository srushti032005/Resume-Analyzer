package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Resume> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM Resume r")
    long countAllResumes();
}
