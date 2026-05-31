package com.example.edumanager.domain.analytics.repository;

import com.example.edumanager.domain.analytics.entity.GradeDistribution;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OlapClassSubjectSummaryRepository 통합 테스트")
class OlapClassSubjectSummaryRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    OlapClassSubjectSummaryRepository repository;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 4, 0);

    @Test
    @DisplayName("TC-1. (school,grade,class_num,semester,subject) 유니크 제약 위반 시 예외가 발생한다")
    void uniqueConstraintViolation() {
        repository.saveAndFlush(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", Subject.MATH));

        assertThatThrownBy(() ->
                repository.saveAndFlush(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", Subject.MATH)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("TC-2. class_num=null(학년전체) 행은 같은 학년의 반별 행들과 공존한다")
    void gradeWideRowCoexistsWithClassRows() {
        repository.save(classRow(School.SUNRIN_HIGH_SCHOOL, 3, null, "2025-1", Subject.MATH)); // 학년 전체
        repository.save(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 1, "2025-1", Subject.MATH));    // 1반
        repository.save(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", Subject.MATH));    // 2반
        // 노이즈: 다른 과목/학기
        repository.save(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-1", Subject.ENGLISH));
        repository.saveAndFlush(classRow(School.SUNRIN_HIGH_SCHOOL, 3, 2, "2025-2", Subject.MATH));

        assertThat(repository.findAll())
                .as("학년전체 + 1반 + 2반 + 다른과목 + 다른학기 = 5건 모두 저장")
                .hasSize(5);
    }

    @Test
    @DisplayName("TC-3. 학교/과목 enum, 분포, 통계값이 그대로 저장·조회된다")
    void enumAndValuesRoundTrip() {
        OlapClassSubjectSummary saved = repository.saveAndFlush(
                OlapClassSubjectSummary.of(School.BUSAN_HIGH_SCHOOL, 1, 4, "2025-2", Subject.SCIENCE,
                        79.1, 98, 45, 28, GradeDistribution.of(3, 10, 8, 5, 2), NOW));

        OlapClassSubjectSummary found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSchool()).isEqualTo(School.BUSAN_HIGH_SCHOOL);
        assertThat(found.getSubject()).isEqualTo(Subject.SCIENCE);
        assertThat(found.getClassNum()).isEqualTo(4);
        assertThat(found.getAvgScore()).isEqualTo(79.1);
        assertThat(found.getMaxScore()).isEqualTo(98);
        assertThat(found.getMinScore()).isEqualTo(45);
        assertThat(found.getStudentCount()).isEqualTo(28);
        assertThat(found.getDistribution().getA()).isEqualTo(3);
        assertThat(found.getDistribution().getF()).isEqualTo(2);
    }

    private OlapClassSubjectSummary classRow(School school, int grade, Integer classNum, String semester, Subject subject) {
        return OlapClassSubjectSummary.of(school, grade, classNum, semester, subject,
                80.0, 100, 60, 20, GradeDistribution.of(5, 5, 5, 3, 2), NOW);
    }
}
