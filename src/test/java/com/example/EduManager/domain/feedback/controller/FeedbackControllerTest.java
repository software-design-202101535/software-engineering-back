package com.example.EduManager.domain.feedback.controller;

import com.example.EduManager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.FeedbackResponse;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.EduManager.domain.feedback.entity.FeedbackCategory;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.facade.FeedbackOperationFacade;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
@DisplayName("FeedbackController 슬라이스 테스트")
class FeedbackControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean FeedbackOperationFacade feedbackOperationFacade;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserDetailsImpl teacher;

    @BeforeEach
    void setUp() {
        teacher = UserDetailsImpl.create(1L, Role.TEACHER);
    }

    private FeedbackResponse stubResponse() {
        return FeedbackResponse.ofForTest(1L);
    }

    @Nested
    @DisplayName("1. getList")
    class GetList {

        @Test
        @DisplayName("TC-1-1. 인증됨 → 200, 배열 반환")
        void success() throws Exception {
            when(feedbackOperationFacade.getList(eq(1L), eq(null), any())).thenReturn(List.of(stubResponse()));

            mockMvc.perform(get("/api/students/1/feedbacks")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(feedbackOperationFacade).getList(eq(1L), eq(null), any(UserDetailsImpl.class));
        }
    }

    @Nested
    @DisplayName("2. create")
    class Create {

        @Test
        @DisplayName("TC-2-1. 인증됨, 유효한 body → 201")
        void success() throws Exception {
            when(feedbackOperationFacade.create(eq(1L), any(), any())).thenReturn(stubResponse());

            String body = "{\"category\":\"GRADE\",\"date\":\"2025-03-14\",\"content\":\"피드백 내용\"}";

            mockMvc.perform(post("/api/students/1/feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<CreateFeedbackRequest> captor = ArgumentCaptor.forClass(CreateFeedbackRequest.class);
            verify(feedbackOperationFacade).create(eq(1L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals(FeedbackCategory.GRADE, captor.getValue().getCategory());
            assertEquals("피드백 내용", captor.getValue().getContent());
        }
    }

    @Nested
    @DisplayName("3. update")
    class Update {

        @Test
        @DisplayName("TC-3-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(feedbackOperationFacade.update(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            String body = "{\"category\":\"GRADE\",\"date\":\"2025-03-14\",\"content\":\"수정 내용\"}";

            mockMvc.perform(put("/api/students/1/feedbacks/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateFeedbackRequest> captor = ArgumentCaptor.forClass(UpdateFeedbackRequest.class);
            verify(feedbackOperationFacade).update(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertEquals("수정 내용", captor.getValue().getContent());
        }
    }

    @Nested
    @DisplayName("4. updateVisibility")
    class UpdateVisibility {

        @Test
        @DisplayName("TC-4-1. 인증됨, 유효한 body → 200")
        void success() throws Exception {
            when(feedbackOperationFacade.updateVisibility(eq(1L), eq(2L), any(), any())).thenReturn(stubResponse());

            mockMvc.perform(patch("/api/students/1/feedbacks/2/visibility")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"studentVisible\":true,\"parentVisible\":false}")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            ArgumentCaptor<UpdateFeedbackVisibilityRequest> captor = ArgumentCaptor.forClass(UpdateFeedbackVisibilityRequest.class);
            verify(feedbackOperationFacade).updateVisibility(eq(1L), eq(2L), captor.capture(), any(UserDetailsImpl.class));
            assertTrue(captor.getValue().isStudentVisible());
            assertFalse(captor.getValue().isParentVisible());
        }
    }

    @Nested
    @DisplayName("5. delete")
    class Delete {

        @Test
        @DisplayName("TC-5-1. 인증됨 → 204")
        void success() throws Exception {
            mockMvc.perform(delete("/api/students/1/feedbacks/2")
                            .with(authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()))))
                    .andExpect(status().isNoContent());

            verify(feedbackOperationFacade).delete(eq(1L), eq(2L), any(UserDetailsImpl.class));
        }
    }
}
