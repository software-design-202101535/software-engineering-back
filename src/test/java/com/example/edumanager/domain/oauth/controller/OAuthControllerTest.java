package com.example.edumanager.domain.oauth.controller;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthLoginRequest;
import com.example.edumanager.domain.oauth.dto.OAuthLoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthRegisterRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @DisplayName("1. POST /kakao")
    class KakaoLogin {

        @Test
        @DisplayName("TC-1-1. 기존 유저 → 200 + isNewUser:false + refreshToken 쿠키")
        void existingUser() throws Exception {
            OAuthLoginRequest request = OAuthLoginRequest.of("code-1");
            LoginResponse login = LoginResponse.ofForTest("access", "refresh");
            when(oauthFacade.loginWithKakao(any())).thenReturn(OAuthLoginResponse.existing(login));

            mockMvc.perform(post("/api/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isNewUser").value(false))
                    .andExpect(jsonPath("$.loginData.accessToken").value("access"))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh")));
        }

        @Test
        @DisplayName("TC-1-2. 신규 유저 → 200 + isNewUser:true + tempToken + 쿠키 없음")
        void newUser() throws Exception {
            OAuthLoginRequest request = OAuthLoginRequest.of("code-2");
            when(oauthFacade.loginWithKakao(any()))
                    .thenReturn(OAuthLoginResponse.newUser("temp-jwt", "new@k.com", "신규"));

            mockMvc.perform(post("/api/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isNewUser").value(true))
                    .andExpect(jsonPath("$.tempToken").value("temp-jwt"))
                    .andExpect(jsonPath("$.email").value("new@k.com"))
                    .andExpect(jsonPath("$.name").value("신규"))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, nullValue()));
        }
    }

    @Nested
    @DisplayName("2. POST /kakao/register")
    class KakaoRegister {

        @Test
        @DisplayName("TC-2-1. 유효한 요청 → 200 + refreshToken 쿠키")
        void success() throws Exception {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", Role.TEACHER, null,
                    OAuthRegisterRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1),
                    null, null);
            when(oauthFacade.registerWithKakao(any()))
                    .thenReturn(LoginResponse.ofForTest("access", "refresh"));

            mockMvc.perform(post("/api/auth/oauth/kakao/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh")));
        }
    }
}
