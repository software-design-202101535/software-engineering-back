package com.example.edumanager.domain.analytics.service;

import com.example.edumanager.domain.analytics.dto.ClassSummaryResponse;
import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.GradeDistribution;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.analytics.repository.OlapClassSubjectSummaryRepository;
import com.example.edumanager.domain.analytics.repository.OlapStudentRankRepository;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService 단위 테스트")
class AnalyticsServiceTest {

    @Mock OlapClassSubjectSummaryRepository classSummaryRepository;
    @Mock OlapStudentRankRepository studentRankRepository;
    @InjectMocks AnalyticsService service;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 4, 30);

    @Test
    @DisplayName("TC-1. 반 통계 응답 — 요청값 echo + 과목/분포/updatedAt 매핑")
    void getClassSummaryMapping() {
        when(classSummaryRepository.findForDashboard(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", null))
                .thenReturn(List.of(OlapClassSubjectSummary.of(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1",
                        Subject.MATH, 79.1, 98, 45, 28, GradeDistribution.of(3, 10, 8, 5, 2), NOW)));

        ClassSummaryResponse response = service.getClassSummary(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", null);

        assertThat(response.getSchool()).isEqualTo(School.SUNRIN_HIGH_SCHOOL);
        assertThat(response.getClassNum()).isEqualTo(2);
        assertThat(response.getUpdatedAt()).isEqualTo(NOW);
        assertThat(response.getSubjects()).hasSize(1);
        ClassSummaryResponse.SubjectSummary math = response.getSubjects().get(0);
        assertThat(math.getSubject()).isEqualTo(Subject.MATH);
        assertThat(math.getAvgScore()).isEqualTo(79.1);
        assertThat(math.getDistribution()).containsEntry("A", 3).containsEntry("F", 2);
    }

    @Test
    @DisplayName("TC-2. 반 통계 — 집계 없으면 subjects 빈 배열, updatedAt null, 요청값은 echo")
    void getClassSummaryEmpty() {
        when(classSummaryRepository.findForDashboard(School.SUNRIN_HIGH_SCHOOL, 3, null, "2025-1", null))
                .thenReturn(List.of());

        ClassSummaryResponse response = service.getClassSummary(School.SUNRIN_HIGH_SCHOOL, 3, null, "2025-1", null);

        assertThat(response.getClassNum()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("TC-3. 학생 석차 — class/grade scope 분리, overall vs 과목별 분리, gradeLevel 계산")
    void getStudentRanksSplit() {
        when(studentRankRepository.findByStudentIdAndSemester(12L, "2025-1")).thenReturn(List.of(
                OlapStudentRank.of(12L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS, 5, 28, 82.1, 88.0, NOW),
                OlapStudentRank.of(12L, "2025-1", OlapStudentRank.OVERALL, AnalyticsScope.CLASS, 3, 28, 89.3, 85.3, NOW),
                OlapStudentRank.of(12L, "2025-1", OlapStudentRank.OVERALL, AnalyticsScope.GRADE, 12, 210, 94.3, 85.3, NOW)));

        StudentRankResponse response = service.getStudentRanks(12L, "2025-1");

        assertThat(response.getClassRanks().getOverall().getRank()).isEqualTo(3);
        assertThat(response.getClassRanks().getSubjects()).hasSize(1);
        StudentRankResponse.SubjectRank math = response.getClassRanks().getSubjects().get(0);
        assertThat(math.getSubject()).isEqualTo("MATH");
        assertThat(math.getGradeLevel()).isEqualTo("B");   // 88 → B
        assertThat(response.getGradeRanks().getOverall().getRank()).isEqualTo(12);
        assertThat(response.getGradeRanks().getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("TC-4. 학생 석차 — 집계 없으면 class/grade 모두 null")
    void getStudentRanksEmpty() {
        when(studentRankRepository.findByStudentIdAndSemester(12L, "2025-1")).thenReturn(List.of());

        StudentRankResponse response = service.getStudentRanks(12L, "2025-1");

        assertThat(response.getClassRanks()).isNull();
        assertThat(response.getGradeRanks()).isNull();
    }
}
