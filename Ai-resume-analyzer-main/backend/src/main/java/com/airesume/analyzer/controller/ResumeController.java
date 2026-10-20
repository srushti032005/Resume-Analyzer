package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.response.ApiResponse;
import com.airesume.analyzer.dto.response.ResumeResponse;
import com.airesume.analyzer.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Resumes", description = "Upload, list, retrieve and analyze resumes")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "Upload a PDF resume and trigger AI analysis")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ResumeResponse>> upload(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Resume uploaded — analysis in progress",
                resumeService.uploadAndAnalyze(file)));
    }

    @Operation(summary = "List the current user's resumes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> myResumes() {
        return ResponseEntity.ok(ApiResponse.ok(resumeService.getMyResumes()));
    }

    @Operation(summary = "Get a single resume with its analysis")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResume(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(resumeService.getResume(id)));
    }

    @Operation(summary = "Re-run AI analysis for a resume")
    @PostMapping("/{id}/reanalyze")
    public ResponseEntity<ApiResponse<ResumeResponse>> reanalyze(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Re-analysis started", resumeService.reanalyze(id)));
    }

    @Operation(summary = "Delete a resume")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.ok(ApiResponse.ok("Resume deleted", null));
    }
}
