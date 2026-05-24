package com.example.edumanager.domain.oauth.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
@Getter
@RequiredArgsConstructor
public class OAuthProperties {

    private final Kakao kakao;

    @Getter
    @RequiredArgsConstructor
    public static class Kakao {
        private final String clientId;
        private final String clientSecret;
        private final String redirectUri;
        private final String frontendRedirectUri;
        private final String authorizationUri;
        private final String tokenUri;
        private final String userInfoUri;
    }
}
