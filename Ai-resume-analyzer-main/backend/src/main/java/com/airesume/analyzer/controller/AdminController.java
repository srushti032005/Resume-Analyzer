package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.response.AdminStatsResponse;
import com.airesume.analyzer.dto.response.ApiResponse;
import com.airesume.analyzer.dto.response.ResumeResponse;
import com.airesume.analyzer.dto.response.UserResponse;
import com.airesume.analyzer.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Administrative platform management (ADMIN only)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> users() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @Operation(summary = "List all uploaded resumes")
    @GetMapping("/resumes")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> resumes() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllResumes()));
    }

    @Operation(summary = "Platform-wide analytics")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getPlatformStats()));
    }

    @Operation(summary = "Delete an abusive user")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    @Operation(summary = "Enable or disable a user account")
    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<Object>> toggleUser(@PathVariable Long id) {
        adminService.toggleUserEnabled(id);
        return ResponseEntity.ok(ApiResponse.ok("User status updated", null));
    }
}
