package com.example.edumanager.domain.devicetoken.repository;

import com.example.edumanager.domain.devicetoken.entity.DeviceToken;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DeviceTokenRepository 통합 테스트")
class DeviceTokenRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired DeviceTokenRepository deviceTokenRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("TC-D-1. token 전역 unique 제약 — 동일 token 중복 저장 시 DataIntegrityViolationException")
    void tokenUniqueConstraint() {
        User u1 = persistUser("u1@test.com");
        User u2 = persistUser("u2@test.com");
        deviceTokenRepository.saveAndFlush(DeviceToken.of(u1, "dup-token"));

        assertThatThrownBy(() -> deviceTokenRepository.saveAndFlush(DeviceToken.of(u2, "dup-token")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("TC-D-2. findAllByUserIdIn — 입력 user 토큰만 반환, 노이즈 user 토큰 제외")
    void findAllByUserIdIn_filtersByUser() {
        User u1 = persistUser("u1@test.com");
        User u2 = persistUser("u2@test.com");
        User noise = persistUser("noise@test.com");
        em.persist(DeviceToken.of(u1, "t-u1-a"));
        em.persist(DeviceToken.of(u1, "t-u1-b"));
        em.persist(DeviceToken.of(u2, "t-u2"));
        em.persist(DeviceToken.of(noise, "t-noise"));
        em.flush();
        em.clear();

        List<DeviceToken> result = deviceTokenRepository.findAllByUserIdIn(List.of(u1.getId(), u2.getId()));

        assertThat(result).extracting(DeviceToken::getToken)
                .containsExactlyInAnyOrder("t-u1-a", "t-u1-b", "t-u2")
                .doesNotContain("t-noise");
    }

    @Test
    @DisplayName("TC-D-3. deleteByUserIdAndToken — 본인 토큰만 삭제, 타 user 동일 키 매칭 안 됨")
    void deleteByUserIdAndToken_ownershipScoped() {
        User me = persistUser("me@test.com");
        User other = persistUser("other@test.com");
        em.persist(DeviceToken.of(me, "my-token"));
        em.persist(DeviceToken.of(other, "other-token"));
        em.flush();

        deviceTokenRepository.deleteByUserIdAndToken(me.getId(), "other-token");
        em.flush();
        em.clear();

        assertThat(deviceTokenRepository.findByToken("my-token")).isPresent();
        assertThat(deviceTokenRepository.findByToken("other-token")).isPresent();

        deviceTokenRepository.deleteByUserIdAndToken(me.getId(), "my-token");
        em.flush();
        em.clear();

        assertThat(deviceTokenRepository.findByToken("my-token")).isEmpty();
        assertThat(deviceTokenRepository.findByToken("other-token")).isPresent();
    }

    @Test
    @DisplayName("TC-D-4. reassign 후 flush → DB의 user_id가 새 owner로 변경됨 (dirty checking 검증)")
    void reassignDirtyChecking() {
        User original = persistUser("orig@test.com");
        User newOwner = persistUser("new@test.com");
        DeviceToken token = deviceTokenRepository.saveAndFlush(DeviceToken.of(original, "tok-reassign"));

        token.reassign(newOwner);
        em.flush();
        em.clear();

        DeviceToken reloaded = deviceTokenRepository.findByToken("tok-reassign").orElseThrow();
        assertThat(reloaded.getUser().getId()).isEqualTo(newOwner.getId());
    }

    @Test
    @DisplayName("TC-D-5. deleteAllByTokenIn — 지정 토큰만 삭제, 노이즈 토큰은 남음")
    void deleteAllByTokenIn() {
        User user = persistUser("u@test.com");
        em.persist(DeviceToken.of(user, "keep-1"));
        em.persist(DeviceToken.of(user, "dead-1"));
        em.persist(DeviceToken.of(user, "dead-2"));
        em.persist(DeviceToken.of(user, "keep-2"));
        em.flush();

        deviceTokenRepository.deleteAllByTokenIn(List.of("dead-1", "dead-2"));
        em.flush();
        em.clear();

        assertThat(deviceTokenRepository.findByToken("dead-1")).isEmpty();
        assertThat(deviceTokenRepository.findByToken("dead-2")).isEmpty();
        assertThat(deviceTokenRepository.findByToken("keep-1")).isPresent();
        assertThat(deviceTokenRepository.findByToken("keep-2")).isPresent();
    }

    private User persistUser(String email) {
        User user = User.of(email, "encoded", "name", Role.STUDENT);
        em.persist(user);
        return user;
    }
}
