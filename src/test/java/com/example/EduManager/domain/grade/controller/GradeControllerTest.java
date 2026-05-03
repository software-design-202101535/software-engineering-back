package com.example.EduManager.domain.grade.controller;

import com.example.EduManager.domain.grade.dto.GradeResponse;
import com.example.EduManager.domain.grade.entity.ExamType;
import com.example.EduManager.domain.grade.entity.Grade;
import com.example.EduManager.domain.grade.entity.GradeLevel;
import com.example.EduManager.domain.grade.entity.Subject;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.GradeOperationFacade;
import com.example.EduManager.global.security.JwtTokenProvider;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.security.exception.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GradeController.class)
@DisplayName("GradeController 슬라이스 테스트")
class GradeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean GradeOperationFacade gradeOperationFacade;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() throws Exception {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
        doAnswer(inv -> {
            HttpServletResponse res = inv.getArgument(1);
            res.setStatus(401);
            return null;
        }).when(jwtAuthenticationEntryPoint).commence(any(), any(), any());
    }

    private GradeResponse stubResponse() {
        Grade grade = mock(Grade.class);
        when(grade.getId()).thenReturn(1L);
        when(grade.getSubject()).thenReturn(Subject.MATH);
        when(grade.getScore()).thenReturn(90);
        when(grade.getGrade()).thenReturn(GradeLevel.A);
        return GradeResponse.of(grade);
    }

    @Nested
    @DisplayName("1. getGrades")
    class GetGrades {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(gradeOperationFacade.getGrades(eq(1L), any(), eq("2025-1"), eq(ExamType.MIDTERM)))
                    .thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/grades")
                            .param("semester", "2025-1")
                            .param("examType", "MIDTERM")
                            .with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(gradeOperationFacade).getGrades(eq(1L), any(UserDetailsImpl.class), eq("2025-1"), eq(ExamType.MIDTERM));
        }

        @Test
        @DisplayName("TC-1-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students/1/grades")
                            .param("semester", "2025-1")
                            .param("examType", "MIDTERM"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("2. batchProcess")
    class BatchProcess {

        @Test
        @DisplayName("TC-2-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(gradeOperationFacade.batchProcess(eq(1L), any(), any())).thenReturn(List.of(stubResponse()));

            String body = "{\"semester\":\"2025-1\",\"examType\":\"MIDTERM\",\"create\":[],\"update\":[],\"delete\":[]}";

            mockMvc.perform(put("/api/students/1/grades/batch")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(gradeOperationFacade).batchProcess(eq(1L), any(UserDetailsImpl.class), any());
        }

        @Test
        @DisplayName("TC-2-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/api/students/1/grades/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"semester\":\"2025-1\",\"examType\":\"MIDTERM\",\"create\":[],\"update\":[],\"delete\":[]}"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
