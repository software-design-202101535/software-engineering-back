package com.example.edumanager.domain.analytics.controller;

import com.example.edumanager.domain.analytics.dto.ClassSummaryResponse;
import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.GradeDistribution;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.analytics.service.AnalyticsService;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.facade.AnalyticsRankFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@DisplayName("AnalyticsController 슬라이스 테스트")
class AnalyticsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AnalyticsService analyticsService;
    @MockitoBean AnalyticsRankFacade analyticsRankFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 4, 30);
    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities());
    }

    @Test
    @DisplayName("TC-1. 반 통계 조회 → 200, 과목/분포(distribution.A) 직렬화")
    void getClassSummary() throws Exception {
        ClassSummaryResponse response = ClassSummaryResponse.of(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1",
                List.of(OlapClassSubjectSummary.of(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", Subject.MATH,
                        79.1, 98, 45, 28, GradeDistribution.of(3, 10, 8, 5, 2), NOW)));
        when(analyticsService.getClassSummary(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", null)).thenReturn(response);

        mockMvc.perform(get("/api/analytics/class-summary")
                        .param("school", "SUNRIN_HIGH_SCHOOL").param("grade", "3")
                        .param("classNum", "2").param("semester", "2025-1")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.school").value("SUNRIN_HIGH_SCHOOL"))
                .andExpect(jsonPath("$.classNum").value(2))
                .andExpect(jsonPath("$.subjects[0].subject").value("MATH"))
                .andExpect(jsonPath("$.subjects[0].avgScore").value(79.1))
                .andExpect(jsonPath("$.subjects[0].distribution.A").value(3));
    }

    @Test
    @DisplayName("TC-2. 학생 석차 조회 → 200, class/grade 키 + gradeLevel 직렬화")
    void getStudentRanks() throws Exception {
        StudentRankResponse response = StudentRankResponse.of(12L, "2025-1", List.of(
                OlapStudentRank.of(12L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS, 5, 28, 82.1, 88.0, NOW),
                OlapStudentRank.of(12L, "2025-1", OlapStudentRank.OVERALL, AnalyticsScope.CLASS, 3, 28, 89.3, 85.3, NOW)));
        when(analyticsRankFacade.getStudentRanks(eq(12L), eq("2025-1"), any())).thenReturn(response);

        mockMvc.perform(get("/api/analytics/students/12/ranks")
                        .param("semester", "2025-1")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(12))
                .andExpect(jsonPath("$.class.overall.rank").value(3))
                .andExpect(jsonPath("$.class.subjects[0].subject").value("MATH"))
                .andExpect(jsonPath("$.class.subjects[0].gradeLevel").value("B"));

        verify(analyticsRankFacade).getStudentRanks(eq(12L), eq("2025-1"), any(UserDetailsImpl.class));
    }
}
