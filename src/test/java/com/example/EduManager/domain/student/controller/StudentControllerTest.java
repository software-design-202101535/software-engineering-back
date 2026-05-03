package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.StudentDetailResponse;
import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.StudentOperationFacade;
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

@WebMvcTest(controllers = StudentController.class)
@DisplayName("StudentController 슬라이스 테스트")
class StudentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean StudentOperationFacade studentOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    @Nested
    @DisplayName("1. getClassStudents")
    class GetClassStudents {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(studentOperationFacade.getClassStudents(any()))
                    .thenReturn(List.of(StudentSummaryResponse.ofForTest(1L)));

            mockMvc.perform(get("/api/students")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(studentOperationFacade).getClassStudents(any(UserDetailsImpl.class));
        }
    }

    @Nested
    @DisplayName("2. getStudentDetail")
    class GetStudentDetail {

        @Test
        @DisplayName("TC-2-1. 인증됨 → 200, 학생 상세 반환")
        void success() throws Exception {
            when(studentOperationFacade.getStudentDetail(eq(1L), any()))
                    .thenReturn(StudentDetailResponse.ofForTest(1L, "김학생"));

            mockMvc.perform(get("/api/students/1")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("김학생"));

            verify(studentOperationFacade).getStudentDetail(eq(1L), any(UserDetailsImpl.class));
        }
    }

    @Nested
    @DisplayName("3. updateStudentDetail")
    class UpdateStudentDetail {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(studentOperationFacade.updateStudentDetail(eq(1L), any(), any()))
                    .thenReturn(StudentDetailResponse.ofForTest(1L, "김학생"));

            mockMvc.perform(patch("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"김학생\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateStudentRequest> captor = ArgumentCaptor.forClass(UpdateStudentRequest.class);
            verify(studentOperationFacade).updateStudentDetail(eq(1L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals("김학생", captor.getValue().getName());
        }
    }
}
