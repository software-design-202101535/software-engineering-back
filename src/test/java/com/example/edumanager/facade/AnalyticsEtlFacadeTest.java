package com.example.edumanager.facade;

import com.example.edumanager.domain.analytics.repository.OlapClassSubjectSummaryRepository;
import com.example.edumanager.domain.analytics.repository.OlapStudentRankRepository;
import com.example.edumanager.domain.grade.service.GradeService;
import com.example.edumanager.domain.user.entity.Role;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsEtlFacade 단위 테스트")
class AnalyticsEtlFacadeTest {

    @Mock GradeService gradeService;
    @Mock OlapClassSubjectSummaryRepository classSummaryRepository;
    @Mock OlapStudentRankRepository studentRankRepository;

    @InjectMocks AnalyticsEtlFacade facade;

    @Test
    @DisplayName("TC-1. 교사 → 인가 통과, 재집계 수행(성적 조회 호출)")
    void rebuildByTeacher_teacher() {
        when(gradeService.getAllForAnalytics()).thenReturn(List.of());

        facade.rebuildByTeacher(UserDetailsImpl.create(1L, Role.TEACHER));

        verify(gradeService).getAllForAnalytics();
    }

    @Test
    @DisplayName("TC-2. 학생 → 403, 재집계 미수행(성적 조회 안 함)")
    void rebuildByTeacher_student() {
        CustomException ex = assertThrows(CustomException.class,
                () -> facade.rebuildByTeacher(UserDetailsImpl.create(1L, Role.STUDENT)));

        assertEquals(ErrorCode.JWT_ACCESS_DENIED, ex.getErrorCode());
        verify(gradeService, never()).getAllForAnalytics();
    }
}
