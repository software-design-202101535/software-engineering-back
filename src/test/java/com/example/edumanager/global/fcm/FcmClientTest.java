package com.example.edumanager.global.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmClient 단위 테스트")
class FcmClientTest {

    @Mock ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @InjectMocks
    FcmClient fcmClient;

    @Nested
    @DisplayName("1. send()")
    class Send {

        @Test
        @DisplayName("TC-1-1. 토큰 리스트가 비어 있음 → FirebaseMessaging 조회조차 안 함")
        void emptyTokens() {
            fcmClient.send(List.of(), "title", "body");

            verifyNoInteractions(firebaseMessagingProvider);
        }

        @Test
        @DisplayName("TC-1-2. FCM 비활성화 (FirebaseMessaging 없음) → 전송 스킵, 예외 없음")
        void fcmDisabled() {
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(null);

            assertDoesNotThrow(() -> fcmClient.send(List.of("tok"), "title", "body"));
        }

        @Test
        @DisplayName("TC-1-3. 정상 → FirebaseMessaging.sendEachForMulticast 호출")
        void success() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);
            when(batchResponse.getSuccessCount()).thenReturn(2);
            when(batchResponse.getFailureCount()).thenReturn(0);

            fcmClient.send(List.of("t1", "t2"), "title", "body");

            verify(messaging).sendEachForMulticast(any(MulticastMessage.class));
        }

        @Test
        @DisplayName("TC-1-4. FirebaseMessagingException 발생 → 로그만, 예외 전파 안 함")
        void exceptionSwallowed() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class)))
                    .thenThrow(mock(FirebaseMessagingException.class));

            assertDoesNotThrow(() -> fcmClient.send(List.of("t1"), "title", "body"));
            verify(messaging).sendEachForMulticast(any(MulticastMessage.class));
        }
    }
}
