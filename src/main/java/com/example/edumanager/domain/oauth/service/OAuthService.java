package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.client.KakaoOAuthClient;
import com.example.edumanager.domain.oauth.client.OAuthUserInfo;
import com.example.edumanager.domain.oauth.entity.OAuthAccount;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.oauth.repository.OAuthAccountRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthAccountRepository oauthAccountRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    public OAuthLoginResult loginWithKakao(String code) {
        OAuthUserInfo info = kakaoOAuthClient.fetchUserInfo(code);
        OAuthProvider provider = OAuthProvider.KAKAO;

        return oauthAccountRepository
                .findUserIdByProviderAndOauthId(provider, info.getOauthId())
                .map(OAuthLoginResult::existing)
                .orElseGet(() -> {
                    String tempToken = jwtTokenProvider.createTempToken(
                            info.getOauthId(), provider, info.getEmail(), info.getName());
                    return OAuthLoginResult.newUser(tempToken, info.getEmail(), info.getName());
                });
    }

    public OAuthTempTokenPayload parseTempToken(String tempToken) {
        Claims claims = jwtTokenProvider.parseTempToken(tempToken);
        return OAuthTempTokenPayload.of(
                claims.getSubject(),
                OAuthProvider.valueOf(claims.get("provider", String.class)),
                claims.get("email", String.class),
                claims.get("name", String.class)
        );
    }

    public OAuthAccount link(User user, OAuthProvider provider, String oauthId) {
        return oauthAccountRepository.save(OAuthAccount.of(user, provider, oauthId));
    }
}
