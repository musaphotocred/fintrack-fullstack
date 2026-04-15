package com.fintrack.auth;

import com.fintrack.auth.dto.*;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.user.UserEntity;
import com.fintrack.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling authentication operations: register, login, refresh, logout.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Registers a new user.
     *
     * @throws BusinessException if email is already taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BusinessException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(request.email(), passwordHash, request.fullName());
        userRepository.save(user);

        log.info("User registered: {}", user.getEmail());

        return createAuthResponse(user);
    }

    /**
     * Authenticates a user and returns tokens.
     *
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());

        return createAuthResponse(user);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @throws BusinessException if refresh token is invalid or expired
     */
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenEntity refreshToken = refreshTokenService.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new BusinessException("Refresh token is expired or revoked");
        }

        UserEntity user = refreshToken.getUser();
        if (user.isDeleted()) {
            throw new BusinessException("User account is deactivated");
        }

        refreshTokenService.revoke(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());

        return createAuthResponse(user);
    }

    /**
     * Logs out a user by revoking their refresh token.
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .ifPresent(refreshTokenService::revoke);

        log.info("User logged out");
    }

    /**
     * Logs out from all devices by revoking all refresh tokens.
     */
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
        log.info("All sessions revoked for user ID: {}", userId);
    }

    private AuthResponse createAuthResponse(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        long expiresIn = jwtService.getAccessTokenTtlSeconds();

        return AuthResponse.of(accessToken, refreshToken, expiresIn, UserResponse.from(user));
    }
}
