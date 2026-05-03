package com.example.EduManager.domain.attendance.controller;

import com.example.EduManager.domain.attendance.dto.AttendanceResponse;
import com.example.EduManager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.EduManager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.EduManager.domain.attendance.entity.AttendanceStatus;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.AttendanceOperationFacade;
import com.example.EduManager.global.security.JwtTokenProvider;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.security.exception.JwtAuthenticationEntryPoint;
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

@WebMvcTest(controllers = AttendanceController.class)
@DisplayName("AttendanceController 슬라이스 테스트")
class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AttendanceOperationFacade attendanceOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private AttendanceResponse stubResponse() {
        return AttendanceResponse.ofForTest(1L);
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
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(attendanceOperationFacade).getList(eq(1L), eq(2025), eq(3), any(UserDetailsImpl.class));
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"ABSENT\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<CreateAttendanceRequest> captor = ArgumentCaptor.forClass(CreateAttendanceRequest.class);
            verify(attendanceOperationFacade).create(eq(1L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals(AttendanceStatus.ABSENT, captor.getValue().getStatus());
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2025-03-14\",\"status\":\"LATE\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateAttendanceRequest> captor = ArgumentCaptor.forClass(UpdateAttendanceRequest.class);
            verify(attendanceOperationFacade).update(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals(AttendanceStatus.LATE, captor.getValue().getStatus());
        }
    }

    @Nested
    @DisplayName("4. delete")
    class Delete {

        @Test
        @DisplayName("TC-4-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/attendance/2")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(attendanceOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }
    }
}
