package com.example.edumanager.domain.grade.controller;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.dto.GradeResponse;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.GradeOperationFacade;
import com.example.edumanager.global.security.JwtTokenProvider;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.security.exception.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GradeController.class)
@DisplayName("GradeController 슬라이스 테스트")
class GradeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean GradeOperationFacade gradeOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private GradeResponse stubResponse() {
        return GradeResponse.ofForTest(1L);
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
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(gradeOperationFacade).getGrades(eq(1L), any(UserDetailsImpl.class), eq("2025-1"), eq(ExamType.MIDTERM));
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            ArgumentCaptor<BatchGradeRequest> captor = ArgumentCaptor.forClass(BatchGradeRequest.class);
            verify(gradeOperationFacade).batchProcess(eq(1L), any(UserDetailsImpl.class), captor.capture());
            assertEquals("2025-1", captor.getValue().getSemester());
            assertEquals(ExamType.MIDTERM, captor.getValue().getExamType());
        }
    }
}
