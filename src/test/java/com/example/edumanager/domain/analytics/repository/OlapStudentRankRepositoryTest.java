package com.example.edumanager.domain.analytics.repository;

import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OlapStudentRankRepository 통합 테스트")
class OlapStudentRankRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    OlapStudentRankRepository repository;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 4, 0);

    @Test
    @DisplayName("TC-1. (student_id,semester,subject,scope) 유니크 제약 위반 시 예외가 발생한다")
    void uniqueConstraintViolation() {
        repository.saveAndFlush(rank(1L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS));

        assertThatThrownBy(() ->
                repository.saveAndFlush(rank(1L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("TC-2. 같은 학생/학기/과목이라도 scope(CLASS/GRADE)와 종합(OVERALL)은 별개 행으로 공존한다")
    void scopeAndOverallCoexist() {
        repository.save(rank(1L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS));        // 수학 반석차
        repository.save(rank(1L, "2025-1", Subject.MATH.name(), AnalyticsScope.GRADE));        // 수학 전교석차
        repository.save(rank(1L, "2025-1", OlapStudentRank.OVERALL, AnalyticsScope.CLASS));    // 종합 반석차
        repository.save(rank(1L, "2025-1", OlapStudentRank.OVERALL, AnalyticsScope.GRADE));    // 종합 전교석차
        // 노이즈: 다른 학생/학기
        repository.save(rank(2L, "2025-1", Subject.MATH.name(), AnalyticsScope.CLASS));
        repository.saveAndFlush(rank(1L, "2025-2", Subject.MATH.name(), AnalyticsScope.CLASS));

        assertThat(repository.findAll()).hasSize(6);
    }

    @Test
    @DisplayName("TC-3. scope enum과 석차/백분위 값이 그대로 저장·조회된다")
    void enumAndValuesRoundTrip() {
        OlapStudentRank saved = repository.saveAndFlush(
                OlapStudentRank.of(12L, "2025-1", Subject.MATH.name(), AnalyticsScope.GRADE,
                        12, 210, 94.3, 88.0, NOW));

        OlapStudentRank found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getScope()).isEqualTo(AnalyticsScope.GRADE);
        assertThat(found.getRankPosition()).isEqualTo(12);
        assertThat(found.getTotalCount()).isEqualTo(210);
        assertThat(found.getPercentile()).isEqualTo(94.3);
        assertThat(found.getAvgScore()).isEqualTo(88.0);
    }

    private OlapStudentRank rank(Long studentId, String semester, String subject, AnalyticsScope scope) {
        return OlapStudentRank.of(studentId, semester, subject, scope, 1, 20, 95.0, 90.0, NOW);
    }
}
