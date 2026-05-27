package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.client.KakaoOAuthClient;
import com.example.edumanager.domain.oauth.client.OAuthUserInfo;
import com.example.edumanager.domain.oauth.entity.OAuthAccount;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.oauth.repository.OAuthAccountRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthService 단위 테스트")
class OAuthServiceTest {

    @Mock OAuthAccountRepository oauthAccountRepository;
    @Mock KakaoOAuthClient kakaoOAuthClient;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks OAuthService oauthService;

    @Mock User user;
    @Mock Claims claims;

    @Nested
    @DisplayName("1. loginWithKakao()")
    class LoginWithKakao {

        @Test
        @DisplayName("TC-1-1. 기존 유저 → existing(userId) 반환, tempToken 발급 안 함")
        void existingUser() {
            OAuthUserInfo info = OAuthUserInfo.of("kakao-1");
            when(kakaoOAuthClient.fetchUserInfo("code-1")).thenReturn(info);
            when(oauthAccountRepository.findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(7L));

            OAuthLoginResult result = oauthService.loginWithKakao("code-1");

            assertAll(
                    () -> assertFalse(result.isNewUser()),
                    () -> assertEquals(7L, result.getExistingUserId()),
                    () -> verify(jwtTokenProvider, never()).createTempToken(any(), any())
            );
        }

        @Test
        @DisplayName("TC-1-2. 신규 유저 → newUser(tempToken) 반환")
        void newUser() {
            OAuthUserInfo info = OAuthUserInfo.of("kakao-2");
            when(kakaoOAuthClient.fetchUserInfo("code-2")).thenReturn(info);
            when(oauthAccountRepository.findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-2"))
                    .thenReturn(Optional.empty());
            when(jwtTokenProvider.createTempToken("kakao-2", OAuthProvider.KAKAO))
                    .thenReturn("temp-jwt");

            OAuthLoginResult result = oauthService.loginWithKakao("code-2");

            assertAll(
                    () -> assertTrue(result.isNewUser()),
                    () -> assertEquals("temp-jwt", result.getTempToken())
            );
        }
    }

    @Nested
    @DisplayName("2. parseTempToken()")
    class ParseTempToken {

        @Test
        @DisplayName("TC-2-1. 정상 → payload 반환")
        void success() {
            when(jwtTokenProvider.parseTempToken("temp-jwt")).thenReturn(claims);
            when(claims.getSubject()).thenReturn("kakao-1");
            when(claims.get("provider", String.class)).thenReturn("KAKAO");

            OAuthTempTokenPayload payload = oauthService.parseTempToken("temp-jwt");

            assertAll(
                    () -> assertEquals("kakao-1", payload.getOauthId()),
                    () -> assertEquals(OAuthProvider.KAKAO, payload.getProvider())
            );
        }
    }

    @Nested
    @DisplayName("3. link()")
    class Link {

        @Test
        @DisplayName("TC-3-1. 정상 → save 호출, 반환 OAuthAccount 필드 일치")
        void success() {
            when(oauthAccountRepository.save(any(OAuthAccount.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            OAuthAccount result = oauthService.link(user, OAuthProvider.KAKAO, "kakao-99");

            assertAll(
                    () -> assertEquals(user, result.getUser()),
                    () -> assertEquals(OAuthProvider.KAKAO, result.getProvider()),
                    () -> assertEquals("kakao-99", result.getOauthId())
            );
        }
    }
}
