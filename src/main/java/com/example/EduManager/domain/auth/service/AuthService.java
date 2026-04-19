package com.example.EduManager.domain.auth.service;

import com.example.EduManager.domain.auth.dto.AuthTokens;
import com.example.EduManager.domain.auth.dto.RefreshResult;
import com.example.EduManager.domain.user.entity.RefreshToken;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.repository.RefreshTokenRepository;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.JwtTokenProvider;
import com.example.EduManager.global.security.exception.JwtAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthTokens authenticate(User user, String rawPassword) {
        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_CREDENTIALS);
        }
        return issueTokens(user);
    }

    public RefreshResult refresh(String refreshToken) {
        RefreshToken stored = getByToken(refreshToken);

        if (stored.isExpired()) {
            deleteRefreshToken(stored.getUser());
            throw new JwtAuthException(ErrorCode.JWT_REFRESH_TOKEN_EXPIRED);
        }

        User user = stored.getUser();
        deleteRefreshToken(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveRefreshToken(user, newRefreshToken);

        return RefreshResult.of(newAccessToken, newRefreshToken);
    }

    public void logout(User user) {
        deleteRefreshToken(user);
    }

    private AuthTokens issueTokens(User user) {
        deleteRefreshToken(user);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveRefreshToken(user, refreshToken);
        return AuthTokens.of(accessToken, refreshToken);
    }

    private void saveRefreshToken(User user, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000);
        refreshTokenRepository.save(RefreshToken.of(user, token, expiresAt));
    }

    private RefreshToken getByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new JwtAuthException(ErrorCode.JWT_REFRESH_TOKEN_NOT_FOUND));
    }

    private void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
