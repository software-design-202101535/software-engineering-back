package com.example.edumanager.domain.user.repository;

import com.example.edumanager.domain.user.entity.RefreshToken;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("RefreshTokenRepository 통합 테스트")
class RefreshTokenRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;

    private User persistUser(String email) {
        return userRepository.save(User.of(email, "pw", "name", Role.TEACHER));
    }

    private String persistToken(User user, String token) {
        refreshTokenRepository.save(RefreshToken.of(user, token, LocalDateTime.now().plusDays(7)));
        return token;
    }

    @Test
    @DisplayName("TC-R-1. deleteByUser → 대상 user 토큰만 삭제, 다른 user 토큰은 유지")
    void deleteByUser_removesOnlyTargetUserTokens() {
        User target = persistUser("target@test.com");
        User noise = persistUser("noise@test.com");
        String targetToken = persistToken(target, "target-token");
        String noiseToken = persistToken(noise, "noise-token");
        em.flush();
        em.clear();

        refreshTokenRepository.deleteByUser(target);

        assertThat(refreshTokenRepository.findByToken(targetToken)).isEmpty();
        assertThat(refreshTokenRepository.findByToken(noiseToken)).isPresent();
    }

    @Test
    @DisplayName("TC-R-2. 토큰 없는 user에 deleteByUser → 0건 삭제, 예외 없음 (동시 refresh race 방어)")
    void deleteByUser_noToken_doesNotThrow() {
        User user = persistUser("notoken@test.com");
        em.flush();
        em.clear();

        assertThatCode(() -> refreshTokenRepository.deleteByUser(user))
                .doesNotThrowAnyException();
    }
}
