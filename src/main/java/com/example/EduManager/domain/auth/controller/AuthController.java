package com.example.EduManager.domain.auth.controller;

import com.example.EduManager.facade.AuthFacade;
import com.example.EduManager.domain.auth.dto.*;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.swagger.AuthApiSpecification;
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
    public ResponseEntity<UserResponse> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.registerTeacher(request));
    }

    @PostMapping("/register/student")
    public ResponseEntity<UserResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.registerStudent(request));
    }

    @PostMapping("/register/parent")
    public ResponseEntity<UserResponse> registerParent(@Valid @RequestBody ParentRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.registerParent(request));
    }

    @PostMapping("/login/school")
    public ResponseEntity<LoginResponse> loginBySchool(@Valid @RequestBody SchoolLoginRequest request,
                                                        HttpServletResponse response) {
        LoginResult result = authFacade.loginBySchool(request);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(LoginResponse.of(result.getUser(), result.getAccessToken()));
    }

    @PostMapping("/login/email")
    public ResponseEntity<LoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
                                                       HttpServletResponse response) {
        LoginResult result = authFacade.loginByEmail(request);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(LoginResponse.of(result.getUser(), result.getAccessToken()));
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
