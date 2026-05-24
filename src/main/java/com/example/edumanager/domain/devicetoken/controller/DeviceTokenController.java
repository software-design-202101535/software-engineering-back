package com.example.edumanager.domain.devicetoken.controller;

import com.example.edumanager.domain.devicetoken.dto.RegisterDeviceTokenRequest;
import com.example.edumanager.facade.DeviceTokenFacade;
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

    private final DeviceTokenFacade deviceTokenFacade;

    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterDeviceTokenRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deviceTokenFacade.register(request, userDetails);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregister(
            @PathVariable String token,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deviceTokenFacade.unregister(token, userDetails);
        return ResponseEntity.noContent().build();
    }
}
