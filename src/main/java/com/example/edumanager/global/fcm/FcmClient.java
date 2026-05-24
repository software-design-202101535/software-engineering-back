package com.example.edumanager.global.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public void send(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.debug("FCM disabled, skip push (tokenCount={})", tokens.size());
            return;
        }
        sendMulticast(messaging, tokens, title, body);
    }

    private void sendMulticast(FirebaseMessaging messaging, List<String> tokens, String title, String body) {
        try {
            BatchResponse response = messaging.sendEachForMulticast(buildMessage(tokens, title, body));
            log.info("FCM 발송 완료: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 발송 실패: {}", e.getMessage(), e);
        }
    }

    private MulticastMessage buildMessage(List<String> tokens, String title, String body) {
        return MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(tokens)
                .build();
    }
}
