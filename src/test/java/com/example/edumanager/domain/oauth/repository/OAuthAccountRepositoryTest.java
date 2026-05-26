package com.example.edumanager.domain.oauth.repository;

import com.example.edumanager.domain.oauth.entity.OAuthAccount;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.repository.UserRepository;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OAuthAccountRepository 통합 테스트")
class OAuthAccountRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired OAuthAccountRepository oauthAccountRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("1. findUserIdByProviderAndOauthId")
    class FindUserIdByProviderAndOauthId {

        @Test
        @DisplayName("TC-1-1. 노이즈 데이터(다른 oauthId) 무시 + 매칭된 user.id projection 단일 쿼리")
        void foundWithProjection() {
            User userA = userRepository.save(User.ofOAuth("a@k.com", "userA", Role.TEACHER));
            User userB = userRepository.save(User.ofOAuth("b@k.com", "userB", Role.STUDENT));
            oauthAccountRepository.save(OAuthAccount.of(userA, OAuthProvider.KAKAO, "kakao-A"));
            oauthAccountRepository.save(OAuthAccount.of(userB, OAuthProvider.KAKAO, "kakao-B"));
            em.flush();
            em.clear();

            Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
            stats.clear();

            Optional<Long> result = oauthAccountRepository
                    .findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-A");

            assertThat(result).contains(userA.getId());
            assertThat(stats.getPrepareStatementCount())
                    .as("projection 으로 단일 SELECT")
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("TC-1-2. 매칭 없음 → empty")
        void notFound() {
            User user = userRepository.save(User.ofOAuth("a@k.com", "userA", Role.TEACHER));
            oauthAccountRepository.save(OAuthAccount.of(user, OAuthProvider.KAKAO, "kakao-A"));
            em.flush();
            em.clear();

            Optional<Long> result = oauthAccountRepository
                    .findUserIdByProviderAndOauthId(OAuthProvider.KAKAO, "kakao-NOT-EXIST");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("2. 유니크 제약 (provider, oauth_id)")
    class UniqueConstraint {

        @Test
        @DisplayName("TC-2-1. 동일 (provider, oauthId) 중복 → DataIntegrityViolationException")
        void duplicate() {
            User userA = userRepository.save(User.ofOAuth("a@k.com", "userA", Role.TEACHER));
            User userB = userRepository.save(User.ofOAuth("b@k.com", "userB", Role.STUDENT));
            oauthAccountRepository.saveAndFlush(OAuthAccount.of(userA, OAuthProvider.KAKAO, "kakao-DUP"));

            assertThatThrownBy(() -> oauthAccountRepository.saveAndFlush(
                    OAuthAccount.of(userB, OAuthProvider.KAKAO, "kakao-DUP")))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
