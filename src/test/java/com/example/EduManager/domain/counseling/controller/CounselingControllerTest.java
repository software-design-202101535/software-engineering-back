package com.example.EduManager.domain.counseling.controller;

import com.example.EduManager.domain.counseling.dto.CounselingResponse;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.CounselingOperationFacade;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounselingController.class)
@DisplayName("CounselingController 슬라이스 테스트")
class CounselingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CounselingOperationFacade counselingOperationFacade;
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

    private CounselingResponse stubResponse() {
        return CounselingResponse.builder()
                .id(1L)
                .studentId(1L)
                .teacherId(1L)
                .teacherName("홍선생")
                .counselingDate(LocalDate.of(2025, 3, 14))
                .content("상담 내용")
                .sharedWithTeachers(false)
                .createdAt(LocalDateTime.of(2025, 3, 14, 9, 0))
                .build();
    }

    @Nested
    @DisplayName("1. getList")
    class GetList {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(counselingOperationFacade.getList(eq(1L), eq(2025), eq(null), any())).thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/counselings")
                            .param("year", "2025")
                            .with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(counselingOperationFacade).getList(eq(1L), eq(2025), eq(null), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-1-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students/1/counselings").param("year", "2025"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("2. create")
    class Create {

        @Test
        @DisplayName("TC-2-1. 인증됨, 유효한 body → 201")
        void success() throws Exception {
            when(counselingOperationFacade.create(eq(1L), any(), any())).thenReturn(stubResponse());

            String body = "{\"counselingDate\":\"2025-03-14\",\"content\":\"상담 내용\"}";

            mockMvc.perform(post("/api/students/1/counselings")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(counselingOperationFacade).create(eq(1L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-2-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/students/1/counselings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"counselingDate\":\"2025-03-14\",\"content\":\"상담 내용\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("3. update")
    class Update {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(counselingOperationFacade.update(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            String body = "{\"counselingDate\":\"2025-03-14\",\"content\":\"수정 내용\"}";

            mockMvc.perform(put("/api/students/1/counselings/2")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(counselingOperationFacade).update(eq(1L), eq(2L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-3-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(put("/api/students/1/counselings/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"counselingDate\":\"2025-03-14\",\"content\":\"수정 내용\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("4. updateShare")
    class UpdateShare {

        @Test
        @DisplayName("TC-4-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(counselingOperationFacade.updateShare(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(patch("/api/students/1/counselings/2/share")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sharedWithTeachers\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(counselingOperationFacade).updateShare(eq(1L), eq(2L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-4-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(patch("/api/students/1/counselings/2/share")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sharedWithTeachers\":true}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("5. delete")
    class Delete {

        @Test
        @DisplayName("TC-5-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/counselings/2").with(user(teacher)))
                    .andExpect(status().isNoContent());

            verify(counselingOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-5-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/students/1/counselings/2"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
