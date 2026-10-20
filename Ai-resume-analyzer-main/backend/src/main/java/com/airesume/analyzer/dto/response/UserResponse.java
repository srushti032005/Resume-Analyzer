package com.airesume.analyzer.dto.response;

import com.airesume.analyzer.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        boolean enabled,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getFullName(), u.getEmail(),
                u.getRole().name(), u.isEnabled(), u.getCreatedAt());
    }
}
