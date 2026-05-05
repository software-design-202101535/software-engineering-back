package com.example.edumanager.domain.student.service;

import com.example.edumanager.domain.student.dto.UpdateStudentRequest;
import com.example.edumanager.domain.student.entity.ParentStudent;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.repository.ParentStudentRepository;
import com.example.edumanager.domain.student.repository.StudentProfileRepository;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("StudentService 단위 테스트")
class StudentServiceTest {

    @Mock StudentProfileRepository studentProfileRepository;
    @Mock ParentStudentRepository parentStudentRepository;

    @InjectMocks
    StudentService studentService;

    @Mock StudentProfile student;
    @Mock User user;

    @Nested
    @DisplayName("1. updateDetail() - parseBirthDate")
    class UpdateDetail {

        @Test
        @DisplayName("TC-1-1. 생년월일 null → null 전달, updateDetail 호출")
        void nullBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", null, null, null, null);

            studentService.updateDetail(student, request);

            verify(student).updateDetail(null, null, null, null);
        }

        @Test
        @DisplayName("TC-1-2. 유효한 날짜 형식 → LocalDate 변환 후 updateDetail 호출")
        void validBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", "2008-01-01", null, null, null);

            studentService.updateDetail(student, request);

            verify(student).updateDetail(LocalDate.of(2008, 1, 1), null, null, null);
        }

        @Test
        @DisplayName("TC-1-3. 잘못된 날짜 형식 → INVALID_BIRTH_DATE, updateDetail never")
        void invalidBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", "1", null, null, null);

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.updateDetail(student, request));

            assertAll(
                    () -> assertEquals(ErrorCode.INVALID_BIRTH_DATE, ex.getErrorCode()),
                    () -> verify(student, never()).updateDetail(any(), any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("2. getById()")
    class GetById {

        @Test
        @DisplayName("TC-2-1. 성공 → StudentProfile 반환")
        void success() {
            when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(student));

            assertEquals(student, studentService.getById(1L));
        }

        @Test
        @DisplayName("TC-2-2. 없음 → STUDENT_NOT_FOUND")
        void notFound() {
            when(studentProfileRepository.findById(999L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.getById(999L));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("3. getProfileByUser()")
    class GetProfileByUser {

        @Test
        @DisplayName("TC-3-1. 성공 → StudentProfile 반환")
        void success() {
            when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(student));

            assertEquals(student, studentService.getProfileByUser(user));
        }

        @Test
        @DisplayName("TC-3-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(studentProfileRepository.findByUser(user)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.getProfileByUser(user));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("4. createProfile()")
    class CreateProfile {

        @Test
        @DisplayName("TC-4-1. 성공 → StudentProfile.of()로 생성 후 save 호출")
        void success() {
            studentService.createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3, 5);

            ArgumentCaptor<StudentProfile> captor = ArgumentCaptor.forClass(StudentProfile.class);
            verify(studentProfileRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals(user, captor.getValue().getUser()),
                    () -> assertEquals(School.SUNRIN_HIGH_SCHOOL, captor.getValue().getSchool()),
                    () -> assertEquals(2, captor.getValue().getGrade()),
                    () -> assertEquals(3, captor.getValue().getClassNum()),
                    () -> assertEquals(5, captor.getValue().getNumber())
            );
        }
    }

    @Nested
    @DisplayName("5. getParentsByStudentId()")
    class GetParentsByStudentId {

        @Test
        @DisplayName("TC-5-1. 성공 → getById → findAllByStudent → parent 목록 반환")
        void success() {
            ParentStudent parentStudent = mock(ParentStudent.class);
            when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(student));
            when(parentStudentRepository.findAllByStudent(student)).thenReturn(List.of(parentStudent));
            when(parentStudent.getParent()).thenReturn(user);

            List<User> result = studentService.getParentsByStudentId(1L);

            assertEquals(List.of(user), result);
        }
    }

    @Nested
    @DisplayName("6. getProfilesByParent()")
    class GetProfilesByParent {

        @Test
        @DisplayName("TC-6-1. 성공 → findAllByParent → student 목록 반환")
        void success() {
            ParentStudent parentStudent = mock(ParentStudent.class);
            when(parentStudentRepository.findAllByParent(user)).thenReturn(List.of(parentStudent));
            when(parentStudent.getStudent()).thenReturn(student);

            List<StudentProfile> result = studentService.getProfilesByParent(user);

            assertEquals(List.of(student), result);
        }
    }

    @Nested
    @DisplayName("7. getClassStudents()")
    class GetClassStudents {

        @Test
        @DisplayName("TC-7-1. 성공 → repository 위임, 결과 반환")
        void success() {
            when(studentProfileRepository.findAllByGradeAndClassNumAndSchool(2, 3, School.SUNRIN_HIGH_SCHOOL))
                    .thenReturn(List.of(student));

            List<StudentProfile> result = studentService.getClassStudents(2, 3, School.SUNRIN_HIGH_SCHOOL);

            assertAll(
                    () -> verify(studentProfileRepository).findAllByGradeAndClassNumAndSchool(2, 3, School.SUNRIN_HIGH_SCHOOL),
                    () -> assertEquals(List.of(student), result)
            );
        }
    }

    @Nested
    @DisplayName("8. linkParent()")
    class LinkParent {

        @Test
        @DisplayName("TC-8-1. 성공 → ParentStudent.of()로 생성 후 save 호출")
        void success() {
            studentService.linkParent(user, student);

            ArgumentCaptor<ParentStudent> captor = ArgumentCaptor.forClass(ParentStudent.class);
            verify(parentStudentRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals(user, captor.getValue().getParent()),
                    () -> assertEquals(student, captor.getValue().getStudent())
            );
        }
    }
}
