package com.example.edumanager.domain.auth.controller;

import com.example.edumanager.facade.AuthFacade;
import com.example.edumanager.domain.auth.dto.*;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.AuthApiSpecification;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiSpecification {

    private final AuthFacade authFacade;

    @PostMapping("/register/teacher")
    public ResponseEntity<Void> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        authFacade.registerTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register/student")
    public ResponseEntity<Void> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        authFacade.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register/parent")
    public ResponseEntity<Void> registerParent(@Valid @RequestBody ParentRegisterRequest request) {
        authFacade.registerParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login/email")
    public ResponseEntity<LoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
                                                       HttpServletResponse response) {
        LoginResponse loginResponse = authFacade.loginByEmail(request);
        setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response) {
        RefreshResult result = authFacade.refresh(refreshToken);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(TokenRefreshResponse.of(result.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        HttpServletResponse response) {
        authFacade.logout(userDetails.getUserId());
        deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
