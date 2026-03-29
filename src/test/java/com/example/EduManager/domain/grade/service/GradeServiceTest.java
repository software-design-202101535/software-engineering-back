package com.example.EduManager.domain.grade.service;

import com.example.EduManager.domain.grade.dto.BatchGradeRequest;
import com.example.EduManager.domain.grade.entity.ExamType;
import com.example.EduManager.domain.grade.entity.Grade;
import com.example.EduManager.domain.grade.entity.Subject;
import com.example.EduManager.domain.grade.repository.GradeRepository;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeService 단위 테스트")
class GradeServiceTest {

    @Mock
    GradeRepository gradeRepository;

    @InjectMocks
    GradeService gradeService;

    StudentProfile student;

    @BeforeEach
    void setUp() {
        User user = User.of("s@test.com", "pw", "학생", Role.STUDENT, School.SUNRIN_HIGH_SCHOOL, "2025001");
        student = StudentProfile.of(user, 2, 3, 1);
    }

    @Nested
    @DisplayName("1. getGrades()")
    class GetGrades {

        @Test
        @DisplayName("TC-1-1. 정상 조회")
        void success() {
            Grade grade = Grade.of(student, "2025-1", Subject.MATH, 90, ExamType.MIDTERM);
            when(gradeRepository.findAllByStudentAndSemesterAndExamType(student, "2025-1", ExamType.MIDTERM))
                    .thenReturn(List.of(grade));

            List<Grade> result = gradeService.getGrades(student, "2025-1", ExamType.MIDTERM);

            assertAll(
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals(Subject.MATH, result.get(0).getSubject()),
                    () -> assertEquals(90, result.get(0).getScore())
            );
        }
    }

    @Nested
    @DisplayName("2. batchProcess() - 성공")
    class BatchProcessSuccess {

        @Test
        @DisplayName("TC-2-1. create만")
        void createOnly() {
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(BatchGradeRequest.CreateItem.of(Subject.MATH, 90)),
                    List.of(), List.of());

            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findExistingSubjects(student, "2025-1", ExamType.MIDTERM, List.of(Subject.MATH)))
                    .thenReturn(List.of());
            when(gradeRepository.findAllByStudentAndSemesterAndExamType(student, "2025-1", ExamType.MIDTERM))
                    .thenReturn(List.of(Grade.of(student, "2025-1", Subject.MATH, 90, ExamType.MIDTERM)));

            List<Grade> result = gradeService.batchProcess(student, request);

            assertAll(
                    () -> verify(gradeRepository).saveAll(anyList()),
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals(Subject.MATH, result.get(0).getSubject()),
                    () -> assertEquals(90, result.get(0).getScore())
            );
        }

        @Test
        @DisplayName("TC-2-2. update만")
        void updateOnly() {
            Grade existingGrade = Grade.of(student, "2025-1", Subject.MATH, 80, ExamType.MIDTERM);
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(),
                    List.of(BatchGradeRequest.UpdateItem.of(1L, Subject.MATH, 90)),
                    List.of());

            when(gradeRepository.findAllById(List.of(1L))).thenReturn(List.of(existingGrade));
            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findConflictingSubjects(eq(student), eq("2025-1"), eq(ExamType.MIDTERM), eq(List.of(Subject.MATH)), any()))
                    .thenReturn(List.of());
            when(gradeRepository.findAllByStudentAndSemesterAndExamType(student, "2025-1", ExamType.MIDTERM))
                    .thenReturn(List.of(existingGrade));

            gradeService.batchProcess(student, request);

            assertEquals(90, existingGrade.getScore());
        }

        @Test
        @DisplayName("TC-2-3. delete만")
        void deleteOnly() {
            Grade gradeToDelete = Grade.of(student, "2025-1", Subject.MATH, 80, ExamType.MIDTERM);
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(), List.of(), List.of(1L));

            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findAllById(List.of(1L))).thenReturn(List.of(gradeToDelete));
            when(gradeRepository.findAllByStudentAndSemesterAndExamType(student, "2025-1", ExamType.MIDTERM))
                    .thenReturn(List.of());

            gradeService.batchProcess(student, request);

            verify(gradeRepository).deleteAll(List.of(gradeToDelete));
        }

        @Test
        @DisplayName("TC-2-4. 삭제 후 같은 과목 재등록")
        void deleteAndRecreateSameSubject() {
            Grade gradeToDelete = Grade.of(student, "2025-1", Subject.MATH, 80, ExamType.MIDTERM);
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(BatchGradeRequest.CreateItem.of(Subject.MATH, 95)),
                    List.of(), List.of(1L));

            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findAllById(List.of(1L))).thenReturn(List.of(gradeToDelete));
            when(gradeRepository.findExistingSubjects(student, "2025-1", ExamType.MIDTERM, List.of(Subject.MATH)))
                    .thenReturn(List.of());
            when(gradeRepository.findAllByStudentAndSemesterAndExamType(student, "2025-1", ExamType.MIDTERM))
                    .thenReturn(List.of(Grade.of(student, "2025-1", Subject.MATH, 95, ExamType.MIDTERM)));

            List<Grade> result = gradeService.batchProcess(student, request);

            assertAll(
                    () -> verify(gradeRepository).deleteAll(List.of(gradeToDelete)),
                    () -> verify(gradeRepository).saveAll(anyList()),
                    () -> assertEquals(95, result.get(0).getScore())
            );
        }
    }

    @Nested
    @DisplayName("3. batchProcess() - 실패")
    class BatchProcessFail {

        @Test
        @DisplayName("TC-3-1. update에 없는 id → GRADE_NOT_FOUND")
        void failUpdateNotFound() {
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(),
                    List.of(BatchGradeRequest.UpdateItem.of(999L, Subject.MATH, 90)),
                    List.of());
            when(gradeRepository.findAllById(List.of(999L))).thenReturn(List.of());

            CustomException ex = assertThrows(CustomException.class,
                    () -> gradeService.batchProcess(student, request));

            assertEquals(ErrorCode.GRADE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-3-2. delete에 없는 id → GRADE_NOT_FOUND")
        void failDeleteNotFound() {
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(), List.of(), List.of(999L));
            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findAllById(List.of(999L))).thenReturn(List.of());

            CustomException ex = assertThrows(CustomException.class,
                    () -> gradeService.batchProcess(student, request));

            assertEquals(ErrorCode.GRADE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-3-3. create에 이미 존재하는 과목 → GRADE_ALREADY_EXISTS")
        void failCreateDuplicate() {
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(BatchGradeRequest.CreateItem.of(Subject.MATH, 90)),
                    List.of(), List.of());
            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findExistingSubjects(student, "2025-1", ExamType.MIDTERM, List.of(Subject.MATH)))
                    .thenReturn(List.of(Subject.MATH));

            CustomException ex = assertThrows(CustomException.class,
                    () -> gradeService.batchProcess(student, request));

            assertEquals(ErrorCode.GRADE_ALREADY_EXISTS, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-3-4. update 시 다른 성적과 과목 충돌 → GRADE_ALREADY_EXISTS")
        void failUpdateSubjectConflict() {
            Grade existingGrade = Grade.of(student, "2025-1", Subject.ENGLISH, 80, ExamType.MIDTERM);
            BatchGradeRequest request = BatchGradeRequest.of("2025-1", ExamType.MIDTERM,
                    List.of(),
                    List.of(BatchGradeRequest.UpdateItem.of(1L, Subject.MATH, 90)),
                    List.of());

            when(gradeRepository.findAllById(List.of(1L))).thenReturn(List.of(existingGrade));
            when(gradeRepository.findAllById(List.of())).thenReturn(List.of());
            when(gradeRepository.findConflictingSubjects(eq(student), eq("2025-1"), eq(ExamType.MIDTERM), eq(List.of(Subject.MATH)), any()))
                    .thenReturn(List.of(Subject.MATH));

            CustomException ex = assertThrows(CustomException.class,
                    () -> gradeService.batchProcess(student, request));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ALREADY_EXISTS, ex.getErrorCode()),
                    () -> verify(gradeRepository, never()).saveAll(any())
            );
        }
    }
}
