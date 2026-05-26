package com.example.edumanager.domain.oauth.client;

import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(KakaoOAuthClient.class)
@EnableConfigurationProperties(OAuthProperties.class)
@TestPropertySource(properties = {
        "oauth.kakao.client-id=test-client-id",
        "oauth.kakao.client-secret=test-client-secret",
        "oauth.kakao.redirect-uri=http://localhost:3000/oauth/kakao/callback",
        "oauth.kakao.token-uri=" + KakaoOAuthClientTest.TOKEN_URI,
        "oauth.kakao.user-info-uri=" + KakaoOAuthClientTest.USER_INFO_URI
})
@DisplayName("KakaoOAuthClient 슬라이스 테스트")
class KakaoOAuthClientTest {

    static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Autowired
    private KakaoOAuthClient kakaoOAuthClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    @DisplayName("정상 응답: oauthId/email/name 을 OAuthUserInfo 로 매핑한다")
    void fetchUserInfoMapsResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":1234567890,\"kakao_account\":{\"email\":\"u@example.com\",\"profile\":{\"nickname\":\"홍길동\"}}}",
                        MediaType.APPLICATION_JSON));

        OAuthUserInfo info = kakaoOAuthClient.fetchUserInfo("code");

        assertAll(
                () -> assertThat(info.getOauthId()).isEqualTo("1234567890"),
                () -> assertThat(info.getEmail()).isEqualTo("u@example.com"),
                () -> assertThat(info.getName()).isEqualTo("홍길동")
        );
    }

    @Test
    @DisplayName("토큰 엔드포인트 5xx 응답이면 OAUTH_PROVIDER_ERROR")
    void tokenEndpointServerErrorThrowsProviderError() {
        server.expect(requestTo(TOKEN_URI)).andRespond(withServerError());

        assertThatThrownBy(() -> kakaoOAuthClient.fetchUserInfo("code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.OAUTH_PROVIDER_ERROR);
    }

    @Test
    @DisplayName("토큰 응답 바디에 access_token 이 없으면 OAUTH_PROVIDER_ERROR")
    void tokenResponseWithoutAccessTokenThrowsProviderError() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> kakaoOAuthClient.fetchUserInfo("code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.OAUTH_PROVIDER_ERROR);
    }

    @Test
    @DisplayName("유저정보 응답에 id 가 없으면 OAUTH_PROVIDER_ERROR")
    void userInfoWithoutIdThrowsProviderError() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> kakaoOAuthClient.fetchUserInfo("code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.OAUTH_PROVIDER_ERROR);
    }

    @Test
    @DisplayName("kakao_account 미동의(null) 시 email/name 은 null 이지만 oauthId 는 반환된다")
    void userInfoWithoutKakaoAccountReturnsNullEmailAndName() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(USER_INFO_URI))
                .andRespond(withSuccess("{\"id\":42}", MediaType.APPLICATION_JSON));

        OAuthUserInfo info = kakaoOAuthClient.fetchUserInfo("code");

        assertAll(
                () -> assertThat(info.getOauthId()).isEqualTo("42"),
                () -> assertThat(info.getEmail()).isNull(),
                () -> assertThat(info.getName()).isNull()
        );
    }
}
