package com.example.edumanager.global.fcm;

import com.example.edumanager.domain.devicetoken.service.DeviceTokenService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmClient 단위 테스트")
class FcmClientTest {

    @Mock ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    @Mock DeviceTokenService deviceTokenService;

    @InjectMocks
    FcmClient fcmClient;

    @Nested
    @DisplayName("1. send()")
    class Send {

        @Test
        @DisplayName("TC-1-1. 토큰 리스트가 비어 있음 → FirebaseMessaging 조회조차 안 함")
        void emptyTokens() {
            fcmClient.send(List.of(), "title", "body");

            verifyNoInteractions(firebaseMessagingProvider, deviceTokenService);
        }

        @Test
        @DisplayName("TC-1-2. FCM 비활성화 (FirebaseMessaging 없음) → 전송/cleanup 스킵, 예외 없음")
        void fcmDisabled() {
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(null);

            assertDoesNotThrow(() -> fcmClient.send(List.of("tok"), "title", "body"));
            verifyNoInteractions(deviceTokenService);
        }

        @Test
        @DisplayName("TC-1-3. 정상 전송 (실패 0건) → cleanup 호출 없음")
        void successNoFailure() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse ok1 = mock(SendResponse.class);
            SendResponse ok2 = mock(SendResponse.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);
            when(batchResponse.getResponses()).thenReturn(List.of(ok1, ok2));
            when(ok1.isSuccessful()).thenReturn(true);
            when(ok2.isSuccessful()).thenReturn(true);
            when(batchResponse.getSuccessCount()).thenReturn(2);
            when(batchResponse.getFailureCount()).thenReturn(0);

            fcmClient.send(List.of("t1", "t2"), "title", "body");

            verify(messaging).sendEachForMulticast(any(MulticastMessage.class));
            verify(deviceTokenService, never()).removeDeadTokens(any());
        }

        @Test
        @DisplayName("TC-1-4. UNREGISTERED/INVALID 토큰 → 해당 토큰만 removeDeadTokens로 전달")
        void deadTokensCleaned() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse ok = mock(SendResponse.class);
            SendResponse unregistered = mock(SendResponse.class);
            SendResponse invalid = mock(SendResponse.class);
            FirebaseMessagingException unregEx = mock(FirebaseMessagingException.class);
            FirebaseMessagingException invalidEx = mock(FirebaseMessagingException.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);
            when(batchResponse.getResponses()).thenReturn(List.of(ok, unregistered, invalid));
            when(ok.isSuccessful()).thenReturn(true);
            when(unregistered.isSuccessful()).thenReturn(false);
            when(invalid.isSuccessful()).thenReturn(false);
            when(unregistered.getException()).thenReturn(unregEx);
            when(invalid.getException()).thenReturn(invalidEx);
            when(unregEx.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
            when(invalidEx.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);
            when(batchResponse.getSuccessCount()).thenReturn(1);
            when(batchResponse.getFailureCount()).thenReturn(2);

            fcmClient.send(List.of("alive", "dead-unreg", "dead-invalid"), "title", "body");

            verify(deviceTokenService).removeDeadTokens(List.of("dead-unreg", "dead-invalid"));
        }

        @Test
        @DisplayName("TC-1-5. 비-dead 실패 (예: SENDER_ID_MISMATCH) → cleanup 없음")
        void nonDeadFailure() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse failed = mock(SendResponse.class);
            FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);
            when(batchResponse.getResponses()).thenReturn(List.of(failed));
            when(failed.isSuccessful()).thenReturn(false);
            when(failed.getException()).thenReturn(ex);
            when(ex.getMessagingErrorCode()).thenReturn(MessagingErrorCode.SENDER_ID_MISMATCH);
            when(batchResponse.getSuccessCount()).thenReturn(0);
            when(batchResponse.getFailureCount()).thenReturn(1);

            fcmClient.send(List.of("tok"), "title", "body");

            verify(deviceTokenService, never()).removeDeadTokens(any());
        }

        @Test
        @DisplayName("TC-1-6. FirebaseMessagingException 발생 → 로그만, 예외 전파 안 함, cleanup 안 함")
        void exceptionSwallowed() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class)))
                    .thenThrow(mock(FirebaseMessagingException.class));

            assertDoesNotThrow(() -> fcmClient.send(List.of("t1"), "title", "body"));
            verify(deviceTokenService, never()).removeDeadTokens(any());
        }

        @Test
        @DisplayName("TC-1-7. 500개 초과 토큰 → 500단위 chunk로 sendEachForMulticast 여러 번 호출")
        void chunkedSend() throws Exception {
            FirebaseMessaging messaging = mock(FirebaseMessaging.class);
            BatchResponse batchResponse = mock(BatchResponse.class);
            when(firebaseMessagingProvider.getIfAvailable()).thenReturn(messaging);
            when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);
            when(batchResponse.getResponses()).thenReturn(List.of());

            List<String> tokens = new java.util.ArrayList<>();
            for (int i = 0; i < 1200; i++) tokens.add("t-" + i);

            fcmClient.send(tokens, "title", "body");

            verify(messaging, org.mockito.Mockito.times(3))
                    .sendEachForMulticast(any(MulticastMessage.class));
        }
    }
}
