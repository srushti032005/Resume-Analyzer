package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.request.*;
import com.airesume.analyzer.dto.response.AuthResponse;
import com.airesume.analyzer.dto.response.UserResponse;
import com.airesume.analyzer.entity.RefreshToken;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.entity.enums.Role;
import com.airesume.analyzer.exception.BadRequestException;
import com.airesume.analyzer.exception.UnauthorizedException;
import com.airesume.analyzer.repository.RefreshTokenRepository;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final long RESET_TOKEN_TTL_MS = 30 * 60 * 1000L;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(user);
        log.info("Registered new user {}", user.getEmail());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().toLowerCase(), request.password()));
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new UnauthorizedException("This account has been disabled");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not recognised"));
        if (stored.isRevoked() || stored.isExpired()) {
            throw new UnauthorizedException("Refresh token is no longer valid");
        }
        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email().toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            user.setResetToken(token);
            user.setResetTokenExpiry(Instant.now().toEpochMilli() + RESET_TOKEN_TTL_MS);
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
        // Always succeed silently to avoid user enumeration.
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry() < Instant.now().toEpochMilli()) {
            throw new BadRequestException("Reset token has expired");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        log.info("Password reset for {}", user.getEmail());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(
                user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(
                        tokenProvider.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);

        return AuthResponse.of(accessToken, refreshToken,
                tokenProvider.getAccessExpirationMs() / 1000,
                UserResponse.from(user));
    }
}
