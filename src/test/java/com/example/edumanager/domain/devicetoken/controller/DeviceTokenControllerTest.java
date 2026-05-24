package com.example.edumanager.domain.devicetoken.controller;

import com.example.edumanager.domain.devicetoken.dto.RegisterDeviceTokenRequest;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.DeviceTokenFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DeviceTokenController.class)
@DisplayName("DeviceTokenController 슬라이스 테스트")
class DeviceTokenControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DeviceTokenFacade deviceTokenFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl student;

    @BeforeEach
    void setUp() {
        student = UserDetailsImpl.create(10L, Role.STUDENT);
    }

    @Nested
    @DisplayName("1. POST /api/devices/tokens")
    class Register {

        @Test
        @DisplayName("TC-1-1. 인증됨, 유효 body → 204, facade.register 호출 + 토큰 전달 확인")
        void success() throws Exception {
            mockMvc.perform(post("/api/devices/tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"fcm-xyz\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isNoContent());

            ArgumentCaptor<RegisterDeviceTokenRequest> captor =
                    ArgumentCaptor.forClass(RegisterDeviceTokenRequest.class);
            verify(deviceTokenFacade).register(captor.capture(), any(UserDetailsImpl.class));
            assertEquals("fcm-xyz", captor.getValue().getToken());
        }

        @Test
        @DisplayName("TC-1-2. token 빈 문자열 → 400 + INVALID_INPUT_VALUE")
        void blankTokenFails() throws Exception {
            mockMvc.perform(post("/api/devices/tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name").value("INVALID_INPUT_VALUE"));
        }

    }

    @Nested
    @DisplayName("2. DELETE /api/devices/tokens/{token}")
    class Unregister {

        @Test
        @DisplayName("TC-2-1. 인증됨 → 204, facade.unregister(token, userDetails) 호출")
        void success() throws Exception {
            mockMvc.perform(delete("/api/devices/tokens/fcm-abc")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(deviceTokenFacade).unregister(eq("fcm-abc"), any(UserDetailsImpl.class));
        }
    }
}
