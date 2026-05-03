package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.StudentDetailResponse;
import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.facade.StudentOperationFacade;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@DisplayName("StudentController 슬라이스 테스트")
class StudentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean StudentOperationFacade studentOperationFacade;
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

    private StudentSummaryResponse stubSummaryResponse() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("김학생");

        StudentProfile profile = mock(StudentProfile.class);
        when(profile.getId()).thenReturn(1L);
        when(profile.getUser()).thenReturn(user);
        when(profile.getGrade()).thenReturn(1);
        when(profile.getClassNum()).thenReturn(1);
        when(profile.getNumber()).thenReturn(1);

        return StudentSummaryResponse.of(profile);
    }

    private StudentDetailResponse stubDetailResponse() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("김학생");

        StudentProfile profile = mock(StudentProfile.class);
        when(profile.getId()).thenReturn(1L);
        when(profile.getUser()).thenReturn(user);
        when(profile.getGrade()).thenReturn(1);
        when(profile.getClassNum()).thenReturn(1);
        when(profile.getNumber()).thenReturn(1);
        when(profile.getBirthDate()).thenReturn(LocalDate.of(2010, 3, 14));
        when(profile.getPhone()).thenReturn("010-1234-5678");
        when(profile.getParentPhone()).thenReturn("010-9876-5432");
        when(profile.getAddress()).thenReturn("서울시 강남구");

        return StudentDetailResponse.of(profile);
    }

    @Nested
    @DisplayName("1. getClassStudents")
    class GetClassStudents {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(studentOperationFacade.getClassStudents(any())).thenReturn(List.of(stubSummaryResponse()));

            mockMvc.perform(get("/api/students").with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(studentOperationFacade).getClassStudents(any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-1-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("2. getStudentDetail")
    class GetStudentDetail {

        @Test
        @DisplayName("TC-2-1. 인증됨 → 200, 학생 상세 반환")
        void success() throws Exception {
            when(studentOperationFacade.getStudentDetail(eq(1L), any())).thenReturn(stubDetailResponse());

            mockMvc.perform(get("/api/students/1").with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("김학생"));

            verify(studentOperationFacade).getStudentDetail(eq(1L), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-2-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("3. updateStudentDetail")
    class UpdateStudentDetail {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(studentOperationFacade.updateStudentDetail(eq(1L), any(), any())).thenReturn(stubDetailResponse());

            mockMvc.perform(patch("/api/students/1")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"김학생\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(studentOperationFacade).updateStudentDetail(eq(1L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-3-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(patch("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"김학생\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
