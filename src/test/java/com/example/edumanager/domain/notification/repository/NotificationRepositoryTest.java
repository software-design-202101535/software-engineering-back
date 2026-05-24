package com.example.edumanager.domain.notification.repository;

import com.example.edumanager.domain.notification.entity.Notification;
import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationRepository 통합 테스트")
class NotificationRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired NotificationRepository notificationRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("TC-N-1. markAllAsReadByUserId — 본인 unread만 read 전환, 본인 read와 타 user unread는 그대로")
    void markAllAsRead_onlyOwnUnread() {
        User me = persistUser("me@test.com");
        User other = persistUser("other@test.com");
        Notification myUnread1 = persistNotification(me, false);
        Notification myUnread2 = persistNotification(me, false);
        Notification myAlreadyRead = persistNotification(me, true);
        Notification otherUnread = persistNotification(other, false);
        em.flush();
        em.clear();

        int updated = notificationRepository.markAllAsReadByUserId(me.getId());

        em.clear();
        assertThat(updated).isEqualTo(2);
        assertThat(notificationRepository.findById(myUnread1.getId()).orElseThrow().isRead()).isTrue();
        assertThat(notificationRepository.findById(myUnread2.getId()).orElseThrow().isRead()).isTrue();
        assertThat(notificationRepository.findById(myAlreadyRead.getId()).orElseThrow().isRead()).isTrue();
        assertThat(notificationRepository.findById(otherUnread.getId()).orElseThrow().isRead()).isFalse();
    }

    @Test
    @DisplayName("TC-N-2. findByUserIdOrderByCreatedAtDesc — 본인 알림만 최신순, 타 user 알림은 제외")
    void findByUserIdOrderByCreatedAtDesc_ownOnlyAndOrdered() {
        User me = persistUser("me@test.com");
        User other = persistUser("other@test.com");
        Notification mineOld = persistNotification(me, false);
        Notification otherNoise = persistNotification(other, false);
        Notification mineNew = persistNotification(me, false);
        em.flush();
        em.clear();

        List<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(me.getId());

        assertThat(result).extracting(Notification::getId)
                .containsExactly(mineNew.getId(), mineOld.getId())
                .doesNotContain(otherNoise.getId());
    }

    @Test
    @DisplayName("TC-N-3. findByIdAndUserId — 본인 거만 Optional, 타 user 거나 미존재는 empty")
    void findByIdAndUserId_ownershipScoped() {
        User me = persistUser("me@test.com");
        User other = persistUser("other@test.com");
        Notification mine = persistNotification(me, false);
        Notification othersNotification = persistNotification(other, false);
        em.flush();
        em.clear();

        Optional<Notification> own = notificationRepository.findByIdAndUserId(mine.getId(), me.getId());
        Optional<Notification> notOwn = notificationRepository.findByIdAndUserId(othersNotification.getId(), me.getId());
        Optional<Notification> missing = notificationRepository.findByIdAndUserId(99999L, me.getId());

        assertThat(own).isPresent();
        assertThat(notOwn).isEmpty();
        assertThat(missing).isEmpty();
    }

    private User persistUser(String email) {
        User user = User.of(email, "encoded", "name", Role.STUDENT);
        em.persist(user);
        return user;
    }

    private Notification persistNotification(User user, boolean read) {
        Notification notification = Notification.of(
                user, NotificationType.GRADE_UPDATED, "t", "m", 1L, ReferenceType.GRADE);
        if (read) notification.markAsRead();
        em.persist(notification);
        return notification;
    }
}
