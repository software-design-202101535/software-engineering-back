package com.example.edumanager.domain.oauth.controller;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthCompleteRequest;
import com.example.edumanager.domain.oauth.dto.OAuthTokenRequest;
import com.example.edumanager.facade.OAuthFacade;
import com.example.edumanager.global.swagger.OAuthApiSpecification;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController implements OAuthApiSpecification {

    private final OAuthFacade oauthFacade;

    @GetMapping("/kakao/authorize")
    public ResponseEntity<Void> authorizeKakao() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(oauthFacade.buildKakaoAuthorizeUrl())
                .build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> callbackKakao(@RequestParam("code") String code) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(oauthFacade.buildFrontendRedirectUrl(code))
                .build();
    }

    @PostMapping("/token")
    public ResponseEntity<LoginResponse> token(@Valid @RequestBody OAuthTokenRequest request,
                                               HttpServletResponse response) {
        LoginResponse loginResponse = oauthFacade.token(request);
        setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/complete")
    public ResponseEntity<LoginResponse> complete(@Valid @RequestBody OAuthCompleteRequest request,
                                                  HttpServletResponse response) {
        LoginResponse loginResponse = oauthFacade.complete(request);
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
