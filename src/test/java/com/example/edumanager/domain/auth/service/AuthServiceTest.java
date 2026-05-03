package com.example.edumanager.domain.auth.service;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.RefreshResult;
import com.example.edumanager.domain.user.entity.RefreshToken;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.repository.RefreshTokenRepository;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.exception.JwtAuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Mock User user;
    @Mock RefreshToken storedToken;

    @Nested
    @DisplayName("1. authenticate()")
    class Authenticate {

        @Test
        @DisplayName("TC-1-1. 성공 → 토큰 발급 및 save 호출")
        void success() {
            when(user.isDeleted()).thenReturn(false);
            when(user.getPassword()).thenReturn("encoded");
            when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
            when(user.getId()).thenReturn(1L);
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access");
            when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh");
            when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(604800000L);

            AuthTokens result = authService.authenticate(user, "raw");

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals("refresh", captor.getValue().getToken()),
                    () -> assertEquals(user, captor.getValue().getUser()),
                    () -> assertEquals("access", result.getAccessToken()),
                    () -> assertEquals("refresh", result.getRefreshToken())
            );
        }

        @Test
        @DisplayName("TC-1-2. 삭제된 유저 → USER_DELETED, 토큰 발급 never")
        void deletedUser() {
            when(user.isDeleted()).thenReturn(true);

            CustomException ex = assertThrows(CustomException.class,
                    () -> authService.authenticate(user, "raw"));

            assertAll(
                    () -> assertEquals(ErrorCode.USER_DELETED, ex.getErrorCode()),
                    () -> verify(jwtTokenProvider, never()).createAccessToken(any())
            );
        }

        @Test
        @DisplayName("TC-1-3. 비밀번호 불일치 → BAD_CREDENTIALS, 토큰 발급 never")
        void badCredentials() {
            when(user.isDeleted()).thenReturn(false);
            when(user.getPassword()).thenReturn("encoded");
            when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

            CustomException ex = assertThrows(CustomException.class,
                    () -> authService.authenticate(user, "wrong"));

            assertAll(
                    () -> assertEquals(ErrorCode.BAD_CREDENTIALS, ex.getErrorCode()),
                    () -> verify(jwtTokenProvider, never()).createAccessToken(any())
            );
        }
    }

    @Nested
    @DisplayName("2. refresh()")
    class Refresh {

        @Test
        @DisplayName("TC-2-1. 성공 → 기존 토큰 삭제 후 새 토큰 발급 및 save")
        void success() {
            when(refreshTokenRepository.findByToken("old-refresh")).thenReturn(Optional.of(storedToken));
            when(storedToken.isExpired()).thenReturn(false);
            when(storedToken.getUser()).thenReturn(user);
            when(user.getId()).thenReturn(1L);
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("new-access");
            when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("new-refresh");
            when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(604800000L);

            RefreshResult result = authService.refresh("old-refresh");

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).deleteByUser(user);
            verify(refreshTokenRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals("new-refresh", captor.getValue().getToken()),
                    () -> assertEquals(user, captor.getValue().getUser()),
                    () -> assertEquals("new-access", result.getAccessToken()),
                    () -> assertEquals("new-refresh", result.getRefreshToken())
            );
        }

        @Test
        @DisplayName("TC-2-2. 만료된 토큰 → JWT_REFRESH_TOKEN_EXPIRED, deleteByUser 호출")
        void expiredToken() {
            when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(storedToken));
            when(storedToken.isExpired()).thenReturn(true);
            when(storedToken.getUser()).thenReturn(user);

            JwtAuthException ex = assertThrows(JwtAuthException.class,
                    () -> authService.refresh("expired"));

            assertAll(
                    () -> assertEquals(ErrorCode.JWT_REFRESH_TOKEN_EXPIRED, ex.getErrorCode()),
                    () -> verify(refreshTokenRepository).deleteByUser(user)
            );
        }

        @Test
        @DisplayName("TC-2-3. 존재하지 않는 토큰 → JWT_REFRESH_TOKEN_NOT_FOUND")
        void notFound() {
            when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

            JwtAuthException ex = assertThrows(JwtAuthException.class,
                    () -> authService.refresh("unknown"));

            assertEquals(ErrorCode.JWT_REFRESH_TOKEN_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("3. logout()")
    class Logout {

        @Test
        @DisplayName("TC-3-1. 성공 → deleteByUser 호출")
        void success() {
            authService.logout(user);

            verify(refreshTokenRepository).deleteByUser(user);
        }
    }
}
