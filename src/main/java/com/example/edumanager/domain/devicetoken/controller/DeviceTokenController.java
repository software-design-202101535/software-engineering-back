package com.example.edumanager.domain.devicetoken.controller;

import com.example.edumanager.domain.devicetoken.dto.RegisterDeviceTokenRequest;
import com.example.edumanager.domain.devicetoken.service.DeviceTokenService;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.DeviceTokenApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices/tokens")
@RequiredArgsConstructor
public class DeviceTokenController implements DeviceTokenApiSpecification {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterDeviceTokenRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deviceTokenService.register(userDetails.getUserId(), request.getToken());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregister(
            @PathVariable String token,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deviceTokenService.unregister(userDetails.getUserId(), token);
        return ResponseEntity.noContent().build();
    }
}
