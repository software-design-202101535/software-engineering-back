package com.example.EduManager.facade;

import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentOperationFacade 단위 테스트")
class StudentOperationFacadeTest {

    @Mock StudentService studentService;
    @Mock TeacherService teacherService;

    @InjectMocks
    StudentOperationFacade facade;

    @Nested
    @DisplayName("1. getClassStudents() - 실패")
    class GetClassStudentsFail {

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT", "ADMIN"})
        @DisplayName("TC-1-1. TEACHER 아닌 역할 → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getClassStudents(userDetails));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(teacherService, never()).getProfileByUserId(any()),
                    () -> verify(studentService, never()).getClassStudents(anyInt(), anyInt(), any())
            );
        }
    }
}
