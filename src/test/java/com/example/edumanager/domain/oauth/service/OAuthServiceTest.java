package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.client.KakaoOAuthClient;
import com.example.edumanager.domain.oauth.client.OAuthUserInfo;
import com.example.edumanager.domain.oauth.dto.OAuthAuthorizeResult;
import com.example.edumanager.domain.oauth.entity.OAuthAccount;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.oauth.repository.OAuthAccountRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthService 단위 테스트")
class OAuthServiceTest {

    @Mock OAuthAccountRepository oauthAccountRepository;
    @Mock OAuthPendingStore pendingStore;
    @Mock KakaoOAuthClient kakaoOAuthClient;

    @InjectMocks OAuthService oauthService;

    @Mock User user;

    @Nested
    @DisplayName("1. handleKakaoCallback()")
    class HandleKakaoCallback {

        @Test
        @DisplayName("TC-1-1. 기존 유저(OAuthAccount 존재) → existingUserId pending + needsInfo=false")
        void existingUser() {
            OAuthUserInfo info = OAuthUserInfo.of("kakao-123", "user@kakao.com", "홍길동");
            when(kakaoOAuthClient.fetchUserInfo("code-xxx")).thenReturn(info);
            when(oauthAccountRepository.findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-123"))
                    .thenReturn(Optional.of(5L));
            when(pendingStore.save(any(OAuthPending.class))).thenReturn("auth-yyy");

            OAuthAuthorizeResult result = oauthService.handleKakaoCallback("code-xxx");

            ArgumentCaptor<OAuthPending> captor = ArgumentCaptor.forClass(OAuthPending.class);
            verify(pendingStore).save(captor.capture());
            OAuthPending saved = captor.getValue();

            assertAll(
                    () -> assertEquals("auth-yyy", result.getAuthCode()),
                    () -> assertFalse(result.isNeedsInfo()),
                    () -> assertEquals("user@kakao.com", result.getEmail()),
                    () -> assertEquals("홍길동", result.getName()),
                    () -> assertFalse(saved.isNewUser()),
                    () -> assertEquals(5L, saved.getExistingUserId()),
                    () -> assertEquals(OAuthProvider.KAKAO, saved.getProvider()),
                    () -> assertEquals("kakao-123", saved.getOauthId())
            );
        }

        @Test
        @DisplayName("TC-1-2. 신규 유저(OAuthAccount 없음) → newUser pending + needsInfo=true")
        void newUser() {
            OAuthUserInfo info = OAuthUserInfo.of("kakao-456", "newuser@kakao.com", "김신규");
            when(kakaoOAuthClient.fetchUserInfo("code-zzz")).thenReturn(info);
            when(oauthAccountRepository.findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-456"))
                    .thenReturn(Optional.empty());
            when(pendingStore.save(any(OAuthPending.class))).thenReturn("auth-www");

            OAuthAuthorizeResult result = oauthService.handleKakaoCallback("code-zzz");

            ArgumentCaptor<OAuthPending> captor = ArgumentCaptor.forClass(OAuthPending.class);
            verify(pendingStore).save(captor.capture());
            OAuthPending saved = captor.getValue();

            assertAll(
                    () -> assertEquals("auth-www", result.getAuthCode()),
                    () -> assertTrue(result.isNeedsInfo()),
                    () -> assertEquals("newuser@kakao.com", result.getEmail()),
                    () -> assertEquals("김신규", result.getName()),
                    () -> assertTrue(saved.isNewUser())
            );
        }
    }

    @Nested
    @DisplayName("2. consumeAuthCode()")
    class ConsumeAuthCode {

        @Test
        @DisplayName("TC-2-1. Store에 있음 → pending 반환")
        void found() {
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "e@k.com", "name");
            when(pendingStore.consume("auth-code")).thenReturn(Optional.of(pending));

            OAuthPending result = oauthService.consumeAuthCode("auth-code");

            assertEquals(pending, result);
        }

        @Test
        @DisplayName("TC-2-2. 없음/만료 → OAUTH_INVALID_AUTHCODE")
        void notFound() {
            when(pendingStore.consume("invalid")).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> oauthService.consumeAuthCode("invalid"));

            assertEquals(ErrorCode.OAUTH_INVALID_AUTHCODE, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("3. link()")
    class Link {

        @Test
        @DisplayName("TC-3-1. 정상 → save 호출, OAuthAccount 필드 일치")
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
