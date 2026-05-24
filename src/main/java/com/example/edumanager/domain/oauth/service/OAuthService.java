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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthAccountRepository oauthAccountRepository;
    private final OAuthPendingStore pendingStore;
    private final KakaoOAuthClient kakaoOAuthClient;

    public OAuthAuthorizeResult handleKakaoCallback(String code) {
        OAuthUserInfo info = kakaoOAuthClient.fetchUserInfo(code);
        OAuthProvider provider = OAuthProvider.KAKAO;

        Optional<Long> existingUserId = oauthAccountRepository
                .findUserIdByProviderAndOauthId(provider, info.getOauthId());

        OAuthPending pending = existingUserId
                .map(userId -> OAuthPending.forExistingUser(
                        provider, info.getOauthId(), userId, info.getEmail(), info.getName()))
                .orElseGet(() -> OAuthPending.forNewUser(
                        provider, info.getOauthId(), info.getEmail(), info.getName()));

        String authCode = pendingStore.save(pending);
        return OAuthAuthorizeResult.of(authCode, pending.isNewUser(), pending.getEmail(), pending.getName());
    }

    public OAuthPending consumeAuthCode(String authCode) {
        return pendingStore.consume(authCode)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_INVALID_AUTHCODE));
    }

    public OAuthAccount link(User user, OAuthProvider provider, String oauthId) {
        return oauthAccountRepository.save(OAuthAccount.of(user, provider, oauthId));
    }
}
