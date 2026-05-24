package com.example.edumanager.domain.oauth.controller;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthCompleteRequest;
import com.example.edumanager.domain.oauth.dto.OAuthTokenRequest;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.OAuthFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OAuthController.class)
@DisplayName("OAuthController 슬라이스 테스트")
class OAuthControllerTest {

    @Autowired MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean OAuthFacade oauthFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Nested
    @DisplayName("1. GET /kakao/authorize")
    class Authorize {

        @Test
        @DisplayName("TC-1-1. 302 + Location 헤더 (카카오 인가 URL)")
        void success() throws Exception {
            when(oauthFacade.buildKakaoAuthorizeUrl())
                    .thenReturn(URI.create("https://kauth.kakao.com/oauth/authorize?client_id=x"));

            mockMvc.perform(get("/api/auth/oauth/kakao/authorize"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, startsWith("https://kauth.kakao.com")));
        }
    }

    @Nested
    @DisplayName("2. GET /kakao/callback")
    class Callback {

        @Test
        @DisplayName("TC-2-1. 302 + Location 헤더 (프론트 redirect URL)")
        void success() throws Exception {
            when(oauthFacade.buildFrontendRedirectUrl("code-1"))
                    .thenReturn(URI.create("http://localhost:5173/oauth/result?authCode=auth-1"));

            mockMvc.perform(get("/api/auth/oauth/kakao/callback").param("code", "code-1"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString("authCode=auth-1")));
        }
    }

    @Nested
    @DisplayName("3. POST /token")
    class Token {

        @Test
        @DisplayName("TC-3-1. 유효한 요청 → 200 + refreshToken 쿠키")
        void success() throws Exception {
            OAuthTokenRequest request = OAuthTokenRequest.of("auth-1");
            when(oauthFacade.token(any())).thenReturn(LoginResponse.ofForTest("access", "refresh"));

            mockMvc.perform(post("/api/auth/oauth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh")));
        }

        @Test
        @DisplayName("TC-3-2. authCode 빈값 → 400")
        void validationFail() throws Exception {
            OAuthTokenRequest request = OAuthTokenRequest.of("");

            mockMvc.perform(post("/api/auth/oauth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("4. POST /complete")
    class Complete {

        @Test
        @DisplayName("TC-4-1. 유효한 요청 → 200 + refreshToken 쿠키")
        void success() throws Exception {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, null,
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1),
                    null, null);
            when(oauthFacade.complete(any())).thenReturn(LoginResponse.ofForTest("access", "refresh"));

            mockMvc.perform(post("/api/auth/oauth/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh")));
        }

        @Test
        @DisplayName("TC-4-2. authCode 빈값 → 400")
        void validationAuthCodeBlank() throws Exception {
            String body = """
                    {
                        "authCode": "",
                        "role": "TEACHER",
                        "teacherInfo": {"school": "SUNRIN_HIGH_SCHOOL", "grade": 1, "classNum": 1},
                        "termsAgreed": true,
                        "privacyAgreed": true
                    }
                    """;

            mockMvc.perform(post("/api/auth/oauth/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC-4-3. termsAgreed=false → 400")
        void validationTermsNotAgreed() throws Exception {
            String body = """
                    {
                        "authCode": "auth-1",
                        "role": "TEACHER",
                        "teacherInfo": {"school": "SUNRIN_HIGH_SCHOOL", "grade": 1, "classNum": 1},
                        "termsAgreed": false,
                        "privacyAgreed": true
                    }
                    """;

            mockMvc.perform(post("/api/auth/oauth/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
