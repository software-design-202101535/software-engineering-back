package com.example.edumanager.domain.oauth.client;

import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final OAuthProperties properties;
    private final RestClient restClient = RestClient.create();

    public OAuthUserInfo fetchUserInfo(String code) {
        String accessToken = exchangeCodeForAccessToken(code);
        return loadUserInfo(accessToken);
    }

    private String exchangeCodeForAccessToken(String code) {
        OAuthProperties.Kakao kakao = properties.getKakao();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakao.getClientId());
        form.add("redirect_uri", kakao.getRedirectUri());
        form.add("code", code);
        if (kakao.getClientSecret() != null && !kakao.getClientSecret().isBlank()) {
            form.add("client_secret", kakao.getClientSecret());
        }

        KakaoTokenResponse response;
        try {
            response = restClient.post()
                    .uri(kakao.getTokenUri())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        if (response == null || response.accessToken() == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }
        return response.accessToken();
    }

    private OAuthUserInfo loadUserInfo(String accessToken) {
        OAuthProperties.Kakao kakao = properties.getKakao();

        KakaoUserResponse response;
        try {
            response = restClient.get()
                    .uri(kakao.getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        if (response == null || response.id() == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        String oauthId = String.valueOf(response.id());
        String email = response.kakaoAccount() != null ? response.kakaoAccount().email() : null;
        String name = response.kakaoAccount() != null && response.kakaoAccount().profile() != null
                ? response.kakaoAccount().profile().nickname()
                : null;

        return OAuthUserInfo.of(oauthId, email, name);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoUserResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record KakaoAccount(
                String email,
                Profile profile
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            private record Profile(String nickname) {
            }
        }
    }
}
