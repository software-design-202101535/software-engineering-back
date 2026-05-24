package com.example.edumanager.domain.oauth.controller;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.client.OAuthProperties;
import com.example.edumanager.domain.oauth.dto.OAuthAuthorizeResult;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController implements OAuthApiSpecification {

    private final OAuthFacade oauthFacade;
    private final OAuthProperties oauthProperties;

    @GetMapping("/kakao/authorize")
    public ResponseEntity<Void> authorizeKakao() {
        OAuthProperties.Kakao kakao = oauthProperties.getKakao();
        String url = UriComponentsBuilder.fromUriString(kakao.getAuthorizationUri())
                .queryParam("client_id", kakao.getClientId())
                .queryParam("redirect_uri", kakao.getRedirectUri())
                .queryParam("response_type", "code")
                .build()
                .toUriString();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> callbackKakao(@RequestParam("code") String code) {
        OAuthAuthorizeResult result = oauthFacade.handleKakaoCallback(code);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(oauthProperties.getKakao().getFrontendRedirectUri())
                .queryParam("authCode", result.getAuthCode());

        if (result.isNeedsInfo()) {
            builder.queryParam("needsInfo", "true");
            if (result.getEmail() != null) {
                builder.queryParam("email", result.getEmail());
            }
            if (result.getName() != null) {
                builder.queryParam("name", result.getName());
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(builder.build().toUriString()))
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
