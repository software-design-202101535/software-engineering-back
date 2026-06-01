package com.example.edumanager.domain.analytics.controller;

import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.AnalyticsEtlFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsEtlController.class)
@DisplayName("AnalyticsEtlController 슬라이스 테스트")
class AnalyticsEtlControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AnalyticsEtlFacade analyticsEtlFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 인가(교사 한정)는 facade 책임이라 facade 단위테스트에서 검증한다.
    // 컨트롤러는 인증된 요청을 facade.rebuildByTeacher 로 위임하는지만 본다.
    @Test
    @DisplayName("TC-1. 인증 요청 → 200, facade.rebuildByTeacher 로 위임")
    void rebuildDelegatesToFacade() throws Exception {
        UserDetailsImpl user = UserDetailsImpl.create(1L, Role.TEACHER);

        mockMvc.perform(post("/api/analytics/etl/rebuild")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))))
                .andExpect(status().isOk());

        verify(analyticsEtlFacade).rebuildByTeacher(any());
    }
}
