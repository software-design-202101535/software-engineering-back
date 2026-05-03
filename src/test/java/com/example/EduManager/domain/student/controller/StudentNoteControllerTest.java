package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.CreateNoteRequest;
import com.example.EduManager.domain.student.dto.NoteResponse;
import com.example.EduManager.domain.student.dto.UpdateNoteRequest;
import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.StudentNoteOperationFacade;
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

@WebMvcTest(controllers = StudentNoteController.class)
@DisplayName("StudentNoteController 슬라이스 테스트")
class StudentNoteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean StudentNoteOperationFacade studentNoteOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private NoteResponse stubResponse() {
        return NoteResponse.ofForTest(1L);
    }

    @Nested
    @DisplayName("1. getList")
    class GetList {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(studentNoteOperationFacade.getList(eq(1L), eq(null), any())).thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/notes")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(studentNoteOperationFacade).getList(eq(1L), eq(null), any(UserDetailsImpl.class));
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"내용\",\"date\":\"2025-03-14\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<CreateNoteRequest> captor = ArgumentCaptor.forClass(CreateNoteRequest.class);
            verify(studentNoteOperationFacade).create(eq(1L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals(NoteCategory.ACHIEVEMENT, captor.getValue().getCategory());
            assertEquals("내용", captor.getValue().getContent());
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"ACHIEVEMENT\",\"content\":\"수정 내용\",\"date\":\"2025-03-14\"}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateNoteRequest> captor = ArgumentCaptor.forClass(UpdateNoteRequest.class);
            verify(studentNoteOperationFacade).update(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals("수정 내용", captor.getValue().getContent());
        }
    }

    @Nested
    @DisplayName("4. delete")
    class Delete {

        @Test
        @DisplayName("TC-4-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/notes/2")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(studentNoteOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }
    }
}
