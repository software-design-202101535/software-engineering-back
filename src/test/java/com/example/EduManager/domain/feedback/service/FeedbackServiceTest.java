package com.example.EduManager.domain.feedback.service;

import com.example.EduManager.domain.feedback.entity.Feedback;
import com.example.EduManager.domain.feedback.repository.FeedbackRepository;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService 단위 테스트")
class FeedbackServiceTest {

    @Mock
    FeedbackRepository feedbackRepository;

    @InjectMocks
    FeedbackService feedbackService;

    @Nested
    @DisplayName("1. getByIdAndStudentId()")
    class GetByIdAndStudentId {

        @Test
        @DisplayName("TC-1-1. 존재하지 않음 → FEEDBACK_NOT_FOUND")
        void notFound() {
            when(feedbackRepository.findByIdAndStudentId(999L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> feedbackService.getByIdAndStudentId(999L, 1L));

            assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-1-2. 성공 → Feedback 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(feedback));

            assertEquals(feedback, feedbackService.getByIdAndStudentId(1L, 1L));
        }
    }
}
