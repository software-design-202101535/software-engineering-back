package com.example.edumanager.facade;

import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.service.AnalyticsService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsRankFacade 단위 테스트")
class AnalyticsRankFacadeTest {

    @Mock AnalyticsService analyticsService;
    @Mock StudentService studentService;
    @Mock UserService userService;
    @InjectMocks AnalyticsRankFacade facade;

    @Mock StudentProfile studentProfile;
    @Mock User user;

    private static final Long STUDENT_ID = 12L;
    private static final String SEM = "2025-1";

    @Test
    @DisplayName("TC-1. 교사는 전체 열람 가능 — 분석 결과 반환")
    void teacherCanAccess() {
        UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
        StudentRankResponse expected = StudentRankResponse.of(STUDENT_ID, SEM, List.of());
        when(analyticsService.getStudentRanks(STUDENT_ID, SEM)).thenReturn(expected);

        StudentRankResponse result = facade.getStudentRanks(STUDENT_ID, SEM, teacher);

        assertThat(result).isSameAs(expected);
        verify(analyticsService).getStudentRanks(STUDENT_ID, SEM);
    }

    @Test
    @DisplayName("TC-2. 학생 본인은 조회 가능")
    void studentSelfCanAccess() {
        UserDetailsImpl student = UserDetailsImpl.create(5L, Role.STUDENT);
        when(userService.getById(5L)).thenReturn(user);
        when(studentService.getProfileByUser(user)).thenReturn(studentProfile);
        when(studentProfile.getId()).thenReturn(STUDENT_ID);
        StudentRankResponse expected = StudentRankResponse.of(STUDENT_ID, SEM, List.of());
        when(analyticsService.getStudentRanks(STUDENT_ID, SEM)).thenReturn(expected);

        assertThat(facade.getStudentRanks(STUDENT_ID, SEM, student)).isSameAs(expected);
    }

    @Test
    @DisplayName("TC-3. 다른 학생을 조회하는 학생은 거부")
    void studentOtherDenied() {
        UserDetailsImpl student = UserDetailsImpl.create(5L, Role.STUDENT);
        when(userService.getById(5L)).thenReturn(user);
        when(studentService.getProfileByUser(user)).thenReturn(studentProfile);
        when(studentProfile.getId()).thenReturn(999L);

        assertThatThrownBy(() -> facade.getStudentRanks(STUDENT_ID, SEM, student))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.STUDENT_ACCESS_DENIED);
        verify(analyticsService, never()).getStudentRanks(any(), any());
    }

    @Test
    @DisplayName("TC-4. 연결된 학부모는 조회 가능")
    void parentLinkedCanAccess() {
        UserDetailsImpl parent = UserDetailsImpl.create(7L, Role.PARENT);
        when(user.getId()).thenReturn(7L);
        when(studentService.getParentsByStudentId(STUDENT_ID)).thenReturn(List.of(user));
        StudentRankResponse expected = StudentRankResponse.of(STUDENT_ID, SEM, List.of());
        when(analyticsService.getStudentRanks(STUDENT_ID, SEM)).thenReturn(expected);

        assertThat(facade.getStudentRanks(STUDENT_ID, SEM, parent)).isSameAs(expected);
    }

    @Test
    @DisplayName("TC-5. 연결되지 않은 학부모는 거부")
    void parentUnlinkedDenied() {
        UserDetailsImpl parent = UserDetailsImpl.create(7L, Role.PARENT);
        when(studentService.getParentsByStudentId(STUDENT_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> facade.getStudentRanks(STUDENT_ID, SEM, parent))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.STUDENT_ACCESS_DENIED);
        verify(analyticsService, never()).getStudentRanks(any(), any());
    }
}
