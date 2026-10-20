package com.airesume.analyzer.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String access, String refresh, Long expiresIn, UserResponse user) {
        return new AuthResponse(access, refresh, "Bearer", expiresIn, user);
    }
}
