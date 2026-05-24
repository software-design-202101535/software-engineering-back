package com.example.edumanager.domain.devicetoken.service;

import com.example.edumanager.domain.devicetoken.entity.DeviceToken;
import com.example.edumanager.domain.devicetoken.repository.DeviceTokenRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserService userService;

    public DeviceToken register(Long userId, String token) {
        User user = userService.getById(userId);
        return deviceTokenRepository.findByToken(token)
                .map(existing -> reassignOwner(existing, user))
                .orElseGet(() -> createNewOrRejectConcurrent(user, token));
    }

    public void unregister(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    // afterCommit hook(FcmClient)에서 호출되므로 자체 트랜잭션이 필요해 예외적으로 @Transactional 부여
    @Transactional
    public void removeDeadTokens(List<String> tokens) {
        if (tokens.isEmpty()) return;
        deviceTokenRepository.deleteAllByTokenIn(tokens);
    }

    public List<String> findTokensByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) return List.of();
        return deviceTokenRepository.findAllByUserIdIn(userIds).stream()
                .map(DeviceToken::getToken)
                .toList();
    }

    private DeviceToken reassignOwner(DeviceToken existing, User newOwner) {
        existing.reassign(newOwner);
        return existing;
    }

    private DeviceToken createNewOrRejectConcurrent(User user, String token) {
        try {
            return deviceTokenRepository.saveAndFlush(DeviceToken.of(user, token));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DEVICE_TOKEN_CONCURRENT_REGISTER);
        }
    }
}
