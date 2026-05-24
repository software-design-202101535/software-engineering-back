package com.example.edumanager.domain.devicetoken.service;

import com.example.edumanager.domain.devicetoken.entity.DeviceToken;
import com.example.edumanager.domain.devicetoken.repository.DeviceTokenRepository;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceTokenService 단위 테스트")
class DeviceTokenServiceTest {

    @Mock DeviceTokenRepository deviceTokenRepository;
    @Mock UserService userService;

    @InjectMocks
    DeviceTokenService deviceTokenService;

    @Nested
    @DisplayName("1. register()")
    class Register {

        @Test
        @DisplayName("TC-1-1. 신규 토큰 → saveAndFlush 호출, 새 DeviceToken 반환")
        void newToken() {
            User user = User.of("a@b.com", "pw", "홍길동", Role.STUDENT);
            when(userService.getById(1L)).thenReturn(user);
            when(deviceTokenRepository.findByToken("new-token")).thenReturn(Optional.empty());
            when(deviceTokenRepository.saveAndFlush(any(DeviceToken.class))).thenAnswer(inv -> inv.getArgument(0));

            DeviceToken result = deviceTokenService.register(1L, "new-token");

            ArgumentCaptor<DeviceToken> captor = ArgumentCaptor.forClass(DeviceToken.class);
            verify(deviceTokenRepository).saveAndFlush(captor.capture());
            assertAll(
                    () -> assertEquals(user, captor.getValue().getUser()),
                    () -> assertEquals("new-token", captor.getValue().getToken()),
                    () -> assertEquals(user, result.getUser()),
                    () -> assertEquals("new-token", result.getToken())
            );
        }

        @Test
        @DisplayName("TC-1-2. 기존 토큰 → 소유자 reassign, saveAndFlush 미호출")
        void existingTokenReassigned() {
            User newOwner = User.of("new@b.com", "pw", "신유저", Role.STUDENT);
            DeviceToken existing = mock(DeviceToken.class);
            when(userService.getById(2L)).thenReturn(newOwner);
            when(deviceTokenRepository.findByToken("existing-token")).thenReturn(Optional.of(existing));

            DeviceToken result = deviceTokenService.register(2L, "existing-token");

            assertAll(
                    () -> verify(existing).reassign(newOwner),
                    () -> verify(deviceTokenRepository, never()).saveAndFlush(any()),
                    () -> assertEquals(existing, result)
            );
        }
    }

    @Nested
    @DisplayName("2. unregister()")
    class Unregister {

        @Test
        @DisplayName("TC-2-1. 성공 → repository.deleteByUserIdAndToken 호출")
        void success() {
            deviceTokenService.unregister(1L, "tok-1");

            verify(deviceTokenRepository).deleteByUserIdAndToken(1L, "tok-1");
        }
    }

    @Nested
    @DisplayName("3. findTokensByUserIds()")
    class FindTokensByUserIds {

        @Test
        @DisplayName("TC-3-1. 빈 userId 리스트 → repository 호출 안 함, 빈 리스트 반환")
        void emptyInput() {
            List<String> result = deviceTokenService.findTokensByUserIds(List.of());

            assertAll(
                    () -> assertTrue(result.isEmpty()),
                    () -> verify(deviceTokenRepository, never()).findAllByUserIdIn(any())
            );
        }

        @Test
        @DisplayName("TC-3-2. 성공 → DeviceToken에서 token 문자열만 추출하여 반환")
        void success() {
            DeviceToken token1 = mock(DeviceToken.class);
            DeviceToken token2 = mock(DeviceToken.class);
            when(token1.getToken()).thenReturn("t1");
            when(token2.getToken()).thenReturn("t2");
            when(deviceTokenRepository.findAllByUserIdIn(List.of(1L, 2L))).thenReturn(List.of(token1, token2));

            List<String> result = deviceTokenService.findTokensByUserIds(List.of(1L, 2L));

            assertEquals(List.of("t1", "t2"), result);
        }
    }
}
