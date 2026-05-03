package com.example.edumanager.domain.counseling.controller;

import com.example.edumanager.domain.counseling.dto.CounselingResponse;
import com.example.edumanager.domain.counseling.dto.CreateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.facade.CounselingOperationFacade;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CounselingController.class)
@DisplayName("CounselingController 슬라이스 테스트")
class CounselingControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CounselingOperationFacade counselingOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private CounselingResponse stubResponse() {
        return CounselingResponse.ofForTest(1L);
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
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(counselingOperationFacade).getList(eq(1L), eq(2025), eq(null), any(UserDetailsImpl.class));
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<CreateCounselingRequest> captor = ArgumentCaptor.forClass(CreateCounselingRequest.class);
            verify(counselingOperationFacade).create(eq(1L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals("상담 내용", captor.getValue().getContent());
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateCounselingRequest> captor = ArgumentCaptor.forClass(UpdateCounselingRequest.class);
            verify(counselingOperationFacade).update(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals("수정 내용", captor.getValue().getContent());
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sharedWithTeachers\":true}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateCounselingShareRequest> captor = ArgumentCaptor.forClass(UpdateCounselingShareRequest.class);
            verify(counselingOperationFacade).updateShare(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertTrue(captor.getValue().isSharedWithTeachers());
        }
    }

    @Nested
    @DisplayName("5. delete")
    class Delete {

        @Test
        @DisplayName("TC-5-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/counselings/2")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(counselingOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }
    }
}
