package com.example.edumanager.facade;

import com.example.edumanager.domain.devicetoken.dto.RegisterDeviceTokenRequest;
import com.example.edumanager.domain.devicetoken.service.DeviceTokenService;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeviceTokenFacade {

    private final DeviceTokenService deviceTokenService;

    @Transactional
    public void register(RegisterDeviceTokenRequest request, UserDetailsImpl userDetails) {
        deviceTokenService.register(userDetails.getUserId(), request.getToken());
    }

    @Transactional
    public void unregister(String token, UserDetailsImpl userDetails) {
        deviceTokenService.unregister(userDetails.getUserId(), token);
    }
}
