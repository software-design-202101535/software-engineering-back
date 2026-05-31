package com.example.edumanager.facade;

import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.analytics.repository.OlapClassSubjectSummaryRepository;
import com.example.edumanager.domain.analytics.repository.OlapStudentRankRepository;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.grade.entity.Grade;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.grade.repository.GradeRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnalyticsEtlFacade 통합 테스트")
class AnalyticsEtlFacadeIntegrationTest extends AbstractIntegrationTest {

    @Autowired AnalyticsEtlFacade analyticsEtlFacade;
    @Autowired GradeRepository gradeRepository;
    @Autowired OlapClassSubjectSummaryRepository classSummaryRepository;
    @Autowired OlapStudentRankRepository studentRankRepository;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 4, 30);
    private static final String SEM = "2025-1";

    private Long idA, idB, idC;

    @BeforeEach
    void setUpGrades() {
        // 3학년 2반
        StudentProfile a = insertStudent("a@test.com", School.SUNRIN_HIGH_SCHOOL, 3, 2, 1);
        StudentProfile b = insertStudent("b@test.com", School.SUNRIN_HIGH_SCHOOL, 3, 2, 2);
        // 3학년 1반 (같은 학년 노이즈)
        StudentProfile c = insertStudent("c@test.com", School.SUNRIN_HIGH_SCHOOL, 3, 1, 1);
        // 2학년 (다른 학년 노이즈)
        StudentProfile d = insertStudent("d@test.com", School.SUNRIN_HIGH_SCHOOL, 2, 1, 1);
        idA = a.getId();
        idB = b.getId();
        idC = c.getId();

        // studentA: MATH 80/90 → 85, ENGLISH 70
        saveGrade(a, Subject.MATH, 80, ExamType.MIDTERM);
        saveGrade(a, Subject.MATH, 90, ExamType.FINAL);
        saveGrade(a, Subject.ENGLISH, 70, ExamType.MIDTERM);
        // studentB: MATH 100/100 → 100, ENGLISH 90
        saveGrade(b, Subject.MATH, 100, ExamType.MIDTERM);
        saveGrade(b, Subject.MATH, 100, ExamType.FINAL);
        saveGrade(b, Subject.ENGLISH, 90, ExamType.MIDTERM);
        // studentC(1반): MATH 60
        saveGrade(c, Subject.MATH, 60, ExamType.MIDTERM);
        // studentD(2학년): MATH 50 (노이즈)
        saveGrade(d, Subject.MATH, 50, ExamType.MIDTERM);
    }

    @Test
    @DisplayName("TC-1. 반 단위 과목 집계 — 학생별 점수 평균/분포/인원")
    void classSummaryAggregation() {
        analyticsEtlFacade.rebuild(NOW);

        OlapClassSubjectSummary class2Math = summary(2, Subject.MATH);
        assertThat(class2Math.getAvgScore()).isEqualTo(92.5);   // (85+100)/2
        assertThat(class2Math.getMaxScore()).isEqualTo(100);
        assertThat(class2Math.getMinScore()).isEqualTo(85);
        assertThat(class2Math.getStudentCount()).isEqualTo(2);
        assertThat(class2Math.getDistribution().getA()).isEqualTo(1);  // 100
        assertThat(class2Math.getDistribution().getB()).isEqualTo(1);  // 85
    }

    @Test
    @DisplayName("TC-2. 학년 전체 집계(class_num=null) — 같은 학년 모든 반 포함, 다른 학년 제외")
    void gradeWideSummaryAggregation() {
        analyticsEtlFacade.rebuild(NOW);

        OlapClassSubjectSummary gradeMath = summary(null, Subject.MATH);
        assertThat(gradeMath.getStudentCount()).as("3학년 A,B,C 3명 (2학년 D 제외)").isEqualTo(3);
        assertThat(gradeMath.getAvgScore()).isEqualTo(81.7);   // (85+100+60)/3
        assertThat(gradeMath.getMinScore()).isEqualTo(60);
        assertThat(gradeMath.getDistribution().getD()).isEqualTo(1);  // 60
    }

    @Test
    @DisplayName("TC-3. 과목 석차 — 반/전교 scope, 동점 없는 케이스")
    void subjectRank() {
        analyticsEtlFacade.rebuild(NOW);

        OlapStudentRank bClass = rank(idB, Subject.MATH.name(), AnalyticsScope.CLASS);
        assertThat(bClass.getRankPosition()).isEqualTo(1);
        assertThat(bClass.getTotalCount()).isEqualTo(2);
        assertThat(bClass.getPercentile()).isEqualTo(50.0);   // (1 - 1/2)*100
        assertThat(bClass.getAvgScore()).isEqualTo(100.0);

        assertThat(rank(idA, Subject.MATH.name(), AnalyticsScope.CLASS).getRankPosition()).isEqualTo(2);

        OlapStudentRank cGrade = rank(idC, Subject.MATH.name(), AnalyticsScope.GRADE);
        assertThat(cGrade.getRankPosition()).as("3학년 전교 수학 꼴찌").isEqualTo(3);
        assertThat(cGrade.getTotalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("TC-4. 종합 석차(OVERALL) — 전 과목 평균 기준")
    void overallRank() {
        analyticsEtlFacade.rebuild(NOW);

        OlapStudentRank bOverallGrade = rank(idB, OlapStudentRank.OVERALL, AnalyticsScope.GRADE);
        assertThat(bOverallGrade.getRankPosition()).isEqualTo(1);
        assertThat(bOverallGrade.getTotalCount()).isEqualTo(3);
        assertThat(bOverallGrade.getAvgScore()).isEqualTo(95.0);   // (100+90)/2

        OlapStudentRank aOverallClass = rank(idA, OlapStudentRank.OVERALL, AnalyticsScope.CLASS);
        assertThat(aOverallClass.getAvgScore()).isEqualTo(77.5);   // (85+70)/2
        assertThat(aOverallClass.getRankPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("TC-5. 전체 재계산 — 재실행해도 중복 누적 없이 같은 결과")
    void fullRebuildIsIdempotent() {
        analyticsEtlFacade.rebuild(NOW);
        long firstSummary = classSummaryRepository.count();
        long firstRank = studentRankRepository.count();

        analyticsEtlFacade.rebuild(NOW);

        assertThat(classSummaryRepository.count()).isEqualTo(firstSummary);
        assertThat(studentRankRepository.count()).isEqualTo(firstRank);
    }

    // ---------- 헬퍼 ----------

    private void saveGrade(StudentProfile student, Subject subject, int score, ExamType examType) {
        gradeRepository.save(Grade.of(student, SEM, subject, score, examType));
    }

    private OlapClassSubjectSummary summary(Integer classNum, Subject subject) {
        return classSummaryRepository.findAll().stream()
                .filter(r -> r.getGrade() == 3 && Objects.equals(r.getClassNum(), classNum)
                        && r.getSubject() == subject && r.getSemester().equals(SEM))
                .findFirst().orElseThrow();
    }

    private OlapStudentRank rank(Long studentId, String subject, AnalyticsScope scope) {
        return studentRankRepository.findAll().stream()
                .filter(r -> r.getStudentId().equals(studentId) && r.getSubject().equals(subject)
                        && r.getScope() == scope && r.getSemester().equals(SEM))
                .findFirst().orElseThrow();
    }
}
