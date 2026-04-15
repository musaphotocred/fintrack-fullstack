package com.fintrack.auth.dto;

import com.fintrack.user.UserEntity;

/**
 * Response DTO for user information.
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
