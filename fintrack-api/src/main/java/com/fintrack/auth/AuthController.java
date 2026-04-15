package com.fintrack.auth;

import com.fintrack.auth.dto.*;
import com.fintrack.common.dto.ApiResponse;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.config.RateLimitConfig;
import com.fintrack.user.UserEntity;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {

    private final AuthService authService;
    private final RateLimitConfig rateLimitConfig;

    public AuthController(AuthService authService, RateLimitConfig rateLimitConfig) {
        this.authService = authService;
        this.rateLimitConfig = rateLimitConfig;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        checkRateLimit(rateLimitConfig.resolveRegisterBucket(getClientIp(httpRequest)));

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        checkRateLimit(rateLimitConfig.resolveLoginBucket(getClientIp(httpRequest)));

        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (BadCredentialsException e) {
            throw new BusinessException("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal UserEntity user) {

        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserEntity user) {

        authService.logoutAll(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices"));
    }

    private void checkRateLimit(Bucket bucket) {
        if (!bucket.tryConsume(1)) {
            throw new BusinessException("Too many requests. Please try again later.");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
