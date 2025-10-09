package com.example.client.project.service;

import com.example.client.project.dto.ApiResponse;
import com.example.client.project.dto.JwtResponse;
import com.example.client.project.dto.TokenRefreshRequest;
import com.example.client.project.entity.RefreshToken;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.exception.ResourceException;
import com.example.client.project.exception.ResourceNotFoundException;
import com.example.client.project.repository.RefreshTokenRepository;
import com.example.client.project.repository.UserRepository;
import com.example.client.project.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 7 * 24 * 60 * 60; // 7 days

    // Create new refresh token and save in DB
    public RefreshToken createRefreshToken(UserEntity user) {
        // Check if refresh token already exists for this user
        RefreshToken existingToken = refreshTokenRepository.findByUser(user).orElse(null);

        if (existingToken != null) {
            existingToken.setToken(UUID.randomUUID().toString());
            existingToken.setExpiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS));
            return refreshTokenRepository.save(existingToken);
        }

        // Otherwise, create new one
        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS))
                .build();

        return refreshTokenRepository.save(newToken);
    }


    //  Verify refresh token expiration
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new ResourceException("Refresh token expired. Please login again.");
        }
        return token;
    }

    //  Get new access token using valid refresh token
    public String getNewAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceException("Invalid refresh token"));

        verifyExpiration(token);
        return token.getUser().getEmail();
    }

    //  Generate new JWT using refresh token
    public ApiResponse<JwtResponse> refreshAccessToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    //  Fixed: call verifyExpiration() directly
                    verifyExpiration(refreshToken);

                    UserEntity user = refreshToken.getUser();
                    String accessToken = jwtUtil.generateToken(user.getEmail(), user.getUsername());

                    return ApiResponse.success(
                            "Access token refreshed successfully",
                            new JwtResponse(
                                    accessToken,             // New access token
                                    requestRefreshToken,     // Same refresh token
                                    user.getEmail(),
                                    user.getRole().name()
                            )
                    );
                })
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));
    }
}
