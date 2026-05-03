package com.example.EduManager.domain.auth.controller;

import com.example.EduManager.domain.auth.dto.*;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.AuthFacade;
import com.example.EduManager.global.security.JwtTokenProvider;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.security.exception.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthFacade authFacade;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            HttpServletResponse res = inv.getArgument(1);
            res.setStatus(401);
            return null;
        }).when(jwtAuthenticationEntryPoint).commence(any(), any(), any());
    }

    @Nested
    @DisplayName("1. registerTeacher")
    class RegisterTeacher {

        @Test
        @DisplayName("TC-1-1. 유효한 요청 → 201")
        void success() throws Exception {
            TeacherRegisterRequest request = TeacherRegisterRequest.of(
                    "teacher@test.com", "password1!", "password1!", "홍길동", "서울중학교", 1, 1);

            mockMvc.perform(post("/api/auth/register/teacher")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authFacade).registerTeacher(any(TeacherRegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("2. registerStudent")
    class RegisterStudent {

        @Test
        @DisplayName("TC-1-2. 유효한 요청 → 201")
        void success() throws Exception {
            StudentRegisterRequest request = StudentRegisterRequest.of(
                    "student@test.com", "password1!", "password1!", "김학생", "서울중학교", 1, 1, 1);

            mockMvc.perform(post("/api/auth/register/student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authFacade).registerStudent(any(StudentRegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("3. registerParent")
    class RegisterParent {

        @Test
        @DisplayName("TC-1-3. 유효한 요청 → 201")
        void success() throws Exception {
            ParentRegisterRequest request = ParentRegisterRequest.of(
                    "parent@test.com", "password1!", "password1!", "홍부모", "student@test.com");

            mockMvc.perform(post("/api/auth/register/parent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(authFacade).registerParent(any(ParentRegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("4. loginByEmail")
    class LoginByEmail {

        @Test
        @DisplayName("TC-2-1. 유효한 요청 → 200, accessToken 응답, refreshToken body 미포함, Set-Cookie HttpOnly/Path")
        void success() throws Exception {
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .userId(1L)
                    .email("teacher@test.com")
                    .name("홍길동")
                    .role("TEACHER")
                    .grade(1)
                    .classNum(1)
                    .build();
            when(authFacade.loginByEmail(any())).thenReturn(loginResponse);

            mockMvc.perform(post("/api/auth/login/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"teacher@test.com\",\"password\":\"password1!\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").doesNotExist())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth")));
        }
    }

    @Nested
    @DisplayName("5. refresh")
    class Refresh {

        @Test
        @DisplayName("TC-3-1. refreshToken 쿠키 → 200, 새 accessToken 응답, Set-Cookie 갱신 HttpOnly/Path")
        void success() throws Exception {
            RefreshResult result = RefreshResult.of("new-access-token", "new-refresh-token");
            when(authFacade.refresh("old-refresh-token")).thenReturn(result);

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refreshToken", "old-refresh-token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new-refresh-token")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth")));
        }
    }

    @Nested
    @DisplayName("6. logout")
    class Logout {

        @Test
        @DisplayName("TC-4-1. 인증됨 → 200, authFacade.logout(1L) 호출, Set-Cookie Max-Age=0")
        void success() throws Exception {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, Role.TEACHER);

            mockMvc.perform(post("/api/auth/logout")
                            .with(user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));

            verify(authFacade).logout(1L);
        }

        @Test
        @DisplayName("TC-4-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
