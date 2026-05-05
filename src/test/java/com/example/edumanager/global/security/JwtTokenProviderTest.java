package com.example.edumanager.global.security;

import com.example.edumanager.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
}
