package com.example.edumanager.domain.notification.controller;

import com.example.edumanager.domain.notification.dto.NotificationResponse;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.NotificationFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@DisplayName("NotificationController 슬라이스 테스트")
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean NotificationFacade notificationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl student;

    @BeforeEach
    void setUp() {
        student = UserDetailsImpl.create(10L, Role.STUDENT);
    }

    @Nested
    @DisplayName("1. GET /api/notifications")
    class GetMyNotifications {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 본인 알림 배열 반환")
        void success() throws Exception {
            when(notificationFacade.getMyNotifications(any()))
                    .thenReturn(List.of(NotificationResponse.ofForTest(7L, "GRADE_UPDATED", false)));

            mockMvc.perform(get("/api/notifications")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(7))
                    .andExpect(jsonPath("$[0].type").value("GRADE_UPDATED"))
                    .andExpect(jsonPath("$[0].read").value(false));

            verify(notificationFacade).getMyNotifications(any(UserDetailsImpl.class));
        }

    }

    @Nested
    @DisplayName("2. PATCH /api/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("TC-2-1. 인증됨 → 204, facade.markAsRead(id, userDetails) 호출")
        void success() throws Exception {
            mockMvc.perform(patch("/api/notifications/5/read")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(notificationFacade).markAsRead(eq(5L), any(UserDetailsImpl.class));
        }
    }

    @Nested
    @DisplayName("3. PATCH /api/notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("TC-3-1. 인증됨 → 204, facade.markAllAsRead(userDetails) 호출")
        void success() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all")
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    student, null, student.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(notificationFacade).markAllAsRead(any(UserDetailsImpl.class));
        }
    }
}
