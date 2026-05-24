package com.example.edumanager.domain.devicetoken.service;

import com.example.edumanager.domain.devicetoken.entity.DeviceToken;
import com.example.edumanager.domain.devicetoken.repository.DeviceTokenRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .orElseGet(() -> createNew(user, token));
    }

    public void unregister(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
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

    private DeviceToken createNew(User user, String token) {
        return deviceTokenRepository.save(DeviceToken.of(user, token));
    }
}
