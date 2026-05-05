package com.example.edumanager.domain.student.service;

import com.example.edumanager.domain.student.dto.CreateNoteRequest;
import com.example.edumanager.domain.student.dto.UpdateNoteRequest;
import com.example.edumanager.domain.student.entity.NoteCategory;
import com.example.edumanager.domain.student.entity.StudentNote;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.repository.StudentNoteRepository;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Nested
    @DisplayName("3. save()")
    class Save {

        @Test
        @DisplayName("TC-3-1. repository.save 호출, 결과 반환")
        void success() {
            StudentProfile student = mock(StudentProfile.class);
            TeacherProfile teacher = mock(TeacherProfile.class);
            CreateNoteRequest request = CreateNoteRequest.of(
                    NoteCategory.ACHIEVEMENT, "수상 내역", LocalDate.of(2025, 3, 1));
            StudentNote saved = mock(StudentNote.class);
            when(studentNoteRepository.save(any(StudentNote.class))).thenReturn(saved);

            StudentNote result = studentNoteService.save(student, request, teacher);

            assertAll(
                    () -> verify(studentNoteRepository).save(any(StudentNote.class)),
                    () -> assertEquals(saved, result)
            );
        }
    }

    @Nested
    @DisplayName("4. update()")
    class Update {

        @Test
        @DisplayName("TC-4-1. note.update 호출, 같은 note 반환")
        void success() {
            StudentNote note = mock(StudentNote.class);
            UpdateNoteRequest request = UpdateNoteRequest.of(
                    NoteCategory.VOLUNTEER, "봉사 활동", LocalDate.of(2025, 4, 1));

            StudentNote result = studentNoteService.update(note, request);

            assertAll(
                    () -> verify(note).update(NoteCategory.VOLUNTEER, "봉사 활동", LocalDate.of(2025, 4, 1)),
                    () -> assertEquals(note, result)
            );
        }
    }

    @Nested
    @DisplayName("5. delete()")
    class Delete {

        @Test
        @DisplayName("TC-5-1. repository.delete 호출")
        void success() {
            StudentNote note = mock(StudentNote.class);

            studentNoteService.delete(note);

            verify(studentNoteRepository).delete(note);
        }
    }
}
