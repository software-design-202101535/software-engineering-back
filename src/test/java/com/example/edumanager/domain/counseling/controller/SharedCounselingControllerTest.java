package com.example.edumanager.domain.counseling.controller;

import com.example.edumanager.domain.counseling.dto.SharedCounselingResponse;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.CounselingOperationFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SharedCounselingController.class)
@DisplayName("SharedCounselingController 슬라이스 테스트")
class SharedCounselingControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CounselingOperationFacade counselingOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities());
    }

    @Test
    @DisplayName("TC-1. year + 필터 파라미터 → 200, facade에 위임")
    void success() throws Exception {
        when(counselingOperationFacade.getSharedList(eq(2026), eq(5), eq(1), eq(2), eq("김"), any()))
                .thenReturn(List.of(SharedCounselingResponse.ofForTest(101L)));

        mockMvc.perform(get("/api/counselings/shared")
                        .param("year", "2026")
                        .param("month", "5")
                        .param("grade", "1")
                        .param("classNum", "2")
                        .param("name", "김")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(101));

        verify(counselingOperationFacade)
                .getSharedList(eq(2026), eq(5), eq(1), eq(2), eq("김"), any(UserDetailsImpl.class));
    }

    @Test
    @DisplayName("TC-2. year 누락 → 400, facade 미호출")
    void missingYear() throws Exception {
        mockMvc.perform(get("/api/counselings/shared")
                        .with(authentication(auth())))
                .andExpect(status().isBadRequest());

        verify(counselingOperationFacade, never())
                .getSharedList(anyInt(), any(), any(), any(), any(), any());
    }
}
