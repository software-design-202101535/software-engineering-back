package com.example.edumanager.domain.student.service;

import com.example.edumanager.domain.student.entity.NoteCategory;
import com.example.edumanager.domain.student.entity.StudentNote;
import com.example.edumanager.domain.student.repository.StudentNoteRepository;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentNoteService 단위 테스트")
class StudentNoteServiceTest {

    @Mock
    StudentNoteRepository studentNoteRepository;

    @InjectMocks
    StudentNoteService studentNoteService;

    @Nested
    @DisplayName("1. findByStudentAndCategory()")
    class FindByStudentAndCategory {

        @Test
        @DisplayName("TC-1-1. category == null → findAllByStudentId 호출")
        void nullCategory() {
            when(studentNoteRepository.findAllByStudentId(1L)).thenReturn(List.of());

            studentNoteService.findByStudentAndCategory(1L, null);

            assertAll(
                    () -> verify(studentNoteRepository).findAllByStudentId(1L),
                    () -> verify(studentNoteRepository, never()).findAllByStudentIdAndCategory(any(), any())
            );
        }

        @Test
        @DisplayName("TC-1-2. category = ACHIEVEMENT → findAllByStudentIdAndCategory 호출")
        void withCategory() {
            when(studentNoteRepository.findAllByStudentIdAndCategory(1L, NoteCategory.ACHIEVEMENT))
                    .thenReturn(List.of());

            studentNoteService.findByStudentAndCategory(1L, NoteCategory.ACHIEVEMENT);

            assertAll(
                    () -> verify(studentNoteRepository).findAllByStudentIdAndCategory(1L, NoteCategory.ACHIEVEMENT),
                    () -> verify(studentNoteRepository, never()).findAllByStudentId(any())
            );
        }
    }

    @Nested
    @DisplayName("2. getByIdAndStudentId()")
    class GetByIdAndStudentId {

        @Test
        @DisplayName("TC-2-1. 존재하지 않음 → NOTE_NOT_FOUND")
        void notFound() {
            when(studentNoteRepository.findByIdAndStudentId(999L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentNoteService.getByIdAndStudentId(999L, 1L));

            assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-2-2. 성공 → StudentNote 반환")
        void success() {
            StudentNote note = mock(StudentNote.class);
            when(studentNoteRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(note));

            assertEquals(note, studentNoteService.getByIdAndStudentId(1L, 1L));
        }
    }
}
