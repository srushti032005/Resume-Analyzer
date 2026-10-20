package com.airesume.analyzer.util;

import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.UnauthorizedException;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user no longer exists"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
