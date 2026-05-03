package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.NoteResponse;
import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.student.entity.StudentNote;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.facade.StudentNoteOperationFacade;
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

@WebMvcTest(StudentNoteController.class)
@DisplayName("StudentNoteController 슬라이스 테스트")
class StudentNoteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean StudentNoteOperationFacade studentNoteOperationFacade;
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

    private NoteResponse stubResponse() {
        User teacherUser = mock(User.class);
        when(teacherUser.getId()).thenReturn(1L);

        TeacherProfile teacherProfile = mock(TeacherProfile.class);
        when(teacherProfile.getUser()).thenReturn(teacherUser);

        StudentProfile studentProfile = mock(StudentProfile.class);
        when(studentProfile.getId()).thenReturn(1L);

        StudentNote note = mock(StudentNote.class);
        when(note.getId()).thenReturn(1L);
        when(note.getStudent()).thenReturn(studentProfile);
        when(note.getTeacher()).thenReturn(teacherProfile);
        when(note.getCategory()).thenReturn(NoteCategory.ACHIEVEMENT);
        when(note.getContent()).thenReturn("내용");
        when(note.getDate()).thenReturn(LocalDate.of(2025, 3, 14));
        when(note.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 3, 14, 9, 0));

        return NoteResponse.of(note);
    }

    @Nested
    @DisplayName("1. getList")
    class GetList {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(studentNoteOperationFacade.getList(eq(1L), eq(null), any())).thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/notes").with(user(teacher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(studentNoteOperationFacade).getList(eq(1L), eq(null), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-1-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(get("/api/students/1/notes"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("2. create")
    class Create {

        @Test
        @DisplayName("TC-2-1. 인증됨, 유효한 body → 201")
        void success() throws Exception {
            when(studentNoteOperationFacade.create(eq(1L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(post("/api/students/1/notes")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"내용\",\"date\":\"2025-03-14\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(studentNoteOperationFacade).create(eq(1L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-2-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(post("/api/students/1/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"내용\",\"date\":\"2025-03-14\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("3. update")
    class Update {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(studentNoteOperationFacade.update(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(patch("/api/students/1/notes/2")
                            .with(user(teacher))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"수정 내용\",\"date\":\"2025-03-14\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(studentNoteOperationFacade).update(eq(1L), eq(2L), any(), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-3-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(patch("/api/students/1/notes/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"수정 내용\",\"date\":\"2025-03-14\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("4. delete")
    class Delete {

        @Test
        @DisplayName("TC-4-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/notes/2").with(user(teacher)))
                    .andExpect(status().isNoContent());

            verify(studentNoteOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }

        @Test
        @DisplayName("TC-4-2. 미인증 → 401")
        void unauthorized() throws Exception {
            mockMvc.perform(delete("/api/students/1/notes/2"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
