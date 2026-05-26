package com.example.edumanager.global.fcm;

import com.example.edumanager.domain.devicetoken.service.DeviceTokenService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    private static final int MAX_MULTICAST_SIZE = 500;

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final DeviceTokenService deviceTokenService;

    public void send(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.debug("FCM disabled, skip push (tokenCount={})", tokens.size());
            return;
        }
        sendInChunks(messaging, tokens, title, body);
    }

    private void sendInChunks(FirebaseMessaging messaging, List<String> tokens, String title, String body) {
        for (int from = 0; from < tokens.size(); from += MAX_MULTICAST_SIZE) {
            int to = Math.min(from + MAX_MULTICAST_SIZE, tokens.size());
            sendMulticast(messaging, tokens.subList(from, to), title, body);
        }
    }

    private void sendMulticast(FirebaseMessaging messaging, List<String> chunk, String title, String body) {
        try {
            BatchResponse response = messaging.sendEachForMulticast(buildMessage(chunk, title, body));
            cleanupDeadTokens(response, chunk);
            log.info("FCM 발송: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
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

    private void cleanupDeadTokens(BatchResponse response, List<String> chunk) {
        List<String> dead = collectDeadTokens(response, chunk);
        if (dead.isEmpty()) return;
        deviceTokenService.removeDeadTokens(dead);
        log.info("FCM dead token 삭제: count={}", dead.size());
    }

    private List<String> collectDeadTokens(BatchResponse response, List<String> chunk) {
        List<SendResponse> responses = response.getResponses();
        List<String> dead = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (isDeadResponse(responses.get(i))) {
                dead.add(chunk.get(i));
            }
        }
        return dead;
    }

    private boolean isDeadResponse(SendResponse response) {
        if (response.isSuccessful()) return false;
        FirebaseMessagingException ex = response.getException();
        if (ex == null) return false;
        MessagingErrorCode code = ex.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
