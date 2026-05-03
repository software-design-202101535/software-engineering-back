package com.example.EduManager.domain.attendance.controller;

import com.example.EduManager.domain.attendance.dto.AttendanceResponse;
import com.example.EduManager.domain.attendance.entity.Attendance;
import com.example.EduManager.domain.attendance.entity.AttendanceStatus;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.AttendanceOperationFacade;
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

@WebMvcTest(AttendanceController.class)
@DisplayName("AttendanceController 슬라이스 테스트")
class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AttendanceOperationFacade attendanceOperationFacade;
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

    private AttendanceResponse stubResponse() {
        StudentProfile profile = mock(StudentProfile.class);
        when(profile.getId()).thenReturn(1L);

        Attendance attendance = mock(Attendance.class);
        when(attendance.getId()).thenReturn(1L);
        when(attendance.getStudent()).thenReturn(profile);
        when(attendance.getDate()).thenReturn(LocalDate.of(2025, 3, 14));
        when(attendance.getStatus()).thenReturn(AttendanceStatus.ABSENT);
        when(attendance.getNote()).thenReturn(null);

        return AttendanceResponse.of(attendance);
    }

    @Nested
    @DisplayName("1. getList")
    class GetList {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(attendanceOperationFacade.getList(eq(1L), eq(2025), eq(3), any())).thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/attendance")
                            .param("year", "2025")
                            .param("month", "3")
                            .with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(attendanceOperationFacade).getList(eq(1L), eq(2025), eq(3), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-1-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students/1/attendance")
                            .param("year", "2025")
                            .param("month", "3"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("2. create")
    class Create {

        @Test
        @DisplayName("TC-2-1. 인증됨, 유효한 body → 201")
        void success() throws Exception {
            when(attendanceOperationFacade.create(eq(1L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(post("/api/students/1/attendance")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"ABSENT\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(attendanceOperationFacade).create(eq(1L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-2-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/students/1/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"ABSENT\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("3. update")
    class Update {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(attendanceOperationFacade.update(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(patch("/api/students/1/attendance/2")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"LATE\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(attendanceOperationFacade).update(eq(1L), eq(2L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-3-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(patch("/api/students/1/attendance/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"LATE\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("4. delete")
    class Delete {

        @Test
        @DisplayName("TC-4-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/attendance/2").with(user(teacher)))
                    .andExpect(status().isNoContent());

            verify(attendanceOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-4-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/students/1/attendance/2"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
