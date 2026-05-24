package com.example.edumanager.domain.oauth.controller;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthLoginRequest;
import com.example.edumanager.domain.oauth.dto.OAuthLoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthRegisterRequest;
import com.example.edumanager.facade.OAuthFacade;
import com.example.edumanager.global.swagger.OAuthApiSpecification;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController implements OAuthApiSpecification {

    private final OAuthFacade oauthFacade;

    @PostMapping("/kakao")
    public ResponseEntity<OAuthLoginResponse> kakaoLogin(@Valid @RequestBody OAuthLoginRequest request,
                                                          HttpServletResponse response) {
        OAuthLoginResponse result = oauthFacade.loginWithKakao(request);
        if (!result.isNewUser()) {
            setRefreshTokenCookie(response, result.getLoginData().getRefreshToken());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/kakao/register")
    public ResponseEntity<LoginResponse> kakaoRegister(@Valid @RequestBody OAuthRegisterRequest request,
                                                        HttpServletResponse response) {
        LoginResponse loginResponse = oauthFacade.registerWithKakao(request);
        setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
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
}
