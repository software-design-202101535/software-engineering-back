package com.example.edumanager.global.security;

import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.user.repository.UserRepository;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-1234567890";
    private static final long ACCESS_EXPIRY = 900_000L;
    private static final long REFRESH_EXPIRY = 604_800_000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                mock(UserRepository.class), SECRET, ACCESS_EXPIRY, REFRESH_EXPIRY);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("createRefreshToken: 같은 userId 로 연속 호출해도 jti 덕분에 서로 다른 토큰 발급")
    void createRefreshTokenProducesUniqueTokenPerCall() {
        String t1 = jwtTokenProvider.createRefreshToken(1L);
        String t2 = jwtTokenProvider.createRefreshToken(1L);

        assertThat(t1).isNotEqualTo(t2);
    }

    @Test
    @DisplayName("createTempToken → parseTempToken: oauthId/provider/type 클레임이 그대로 복원된다")
    void tempTokenRoundTripPreservesAllClaims() {
        String oauthId = "kakao-12345";

        String tempToken = jwtTokenProvider.createTempToken(oauthId, OAuthProvider.KAKAO);
        Claims claims = jwtTokenProvider.parseTempToken(tempToken);

        assertAll(
                () -> assertThat(claims.getSubject()).isEqualTo(oauthId),
                () -> assertThat(claims.get("type", String.class)).isEqualTo("TEMP"),
                () -> assertThat(claims.get("provider", String.class)).isEqualTo("KAKAO")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTempTokens")
    @DisplayName("parseTempToken: type 클레임이 없는 AccessToken / 위조·구조불량 토큰은 INVALID_TEMP_TOKEN")
    void parseTempTokenRejectsNonTempTokens(String caseName, String token) {
        assertThatThrownBy(() -> jwtTokenProvider.parseTempToken(token))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TEMP_TOKEN);
    }

    private static Stream<Arguments> invalidTempTokens() {
        JwtTokenProvider helper = new JwtTokenProvider(
                mock(UserRepository.class), SECRET, ACCESS_EXPIRY, REFRESH_EXPIRY);
        helper.init();
        return Stream.of(
                Arguments.of("AccessToken은 type 클레임이 없어 거부", helper.createAccessToken(7L)),
                Arguments.of("구조 불량(garbage)", "not.a.jwt"),
                Arguments.of("빈 문자열", "")
        );
    }
}
