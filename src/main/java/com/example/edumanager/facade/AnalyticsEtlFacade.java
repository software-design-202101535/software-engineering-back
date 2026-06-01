package com.example.edumanager.facade;

import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.GradeDistribution;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.analytics.repository.OlapClassSubjectSummaryRepository;
import com.example.edumanager.domain.analytics.repository.OlapStudentRankRepository;
import com.example.edumanager.domain.grade.entity.Grade;
import com.example.edumanager.domain.grade.entity.GradeLevel;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.grade.service.GradeService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OLAP ETL — 운영 성적을 읽어 분석 테이블(반/학년 집계, 학생 석차)을 전체 재계산한다.
 * 매번 비우고 다시 채우므로(full rebuild) 중복 걱정이 없다.
 *
 * 집계 의미:
 * - 학생의 과목 점수(학기) = 그 과목·학기 시험들의 평균
 * - 반/학년 평균 = 학생별 과목 점수의 평균(학생 동등 가중)
 * - 종합(OVERALL) = 학생의 전 과목 점수 평균
 * - 석차 = scope 내 점수 내림차순, 동점은 표준경쟁순위(1,2,2,4)
 */
@Component
@RequiredArgsConstructor
public class AnalyticsEtlFacade {

    private final GradeService gradeService;
    private final OlapClassSubjectSummaryRepository classSummaryRepository;
    private final OlapStudentRankRepository studentRankRepository;

    /** 수동 트리거(API)용. 교사만 허용하고, 인가 통과 시 스케줄러와 동일한 전체 재집계를 수행한다. */
    @Transactional
    public void rebuildByTeacher(UserDetailsImpl userDetails) {
        if (userDetails.getRole() != Role.TEACHER) {
            throw new CustomException(ErrorCode.JWT_ACCESS_DENIED);
        }
        rebuild(LocalDateTime.now());
    }

    @Transactional
    public void rebuild(LocalDateTime aggregatedAt) {
        List<StudentSubjectScore> scores = toStudentSubjectScores(gradeService.getAllForAnalytics());

        classSummaryRepository.deleteAllInBatch();
        studentRankRepository.deleteAllInBatch();

        classSummaryRepository.saveAll(buildClassSummaries(scores, aggregatedAt));
        studentRankRepository.saveAll(buildStudentRanks(scores, aggregatedAt));
    }

    // ---------- 1. 성적 → 학생별 과목 점수(시험 평균) ----------

    private List<StudentSubjectScore> toStudentSubjectScores(List<Grade> grades) {
        Map<ScoreKey, List<Integer>> grouped = new LinkedHashMap<>();
        for (Grade grade : grades) {
            if (grade.getScore() == null) {
                continue;
            }
            StudentProfile s = grade.getStudent();
            ScoreKey key = new ScoreKey(s.getId(), s.getSchool(), s.getGrade(), s.getClassNum(),
                    grade.getSemester(), grade.getSubject());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(grade.getScore());
        }
        return grouped.entrySet().stream()
                .map(e -> new StudentSubjectScore(e.getKey(), averageInt(e.getValue())))
                .toList();
    }

    // ---------- 2. 반/학년 × 과목 집계 ----------

    private List<OlapClassSubjectSummary> buildClassSummaries(List<StudentSubjectScore> scores, LocalDateTime at) {
        List<OlapClassSubjectSummary> result = new ArrayList<>();
        groupBy(scores, s -> new ClassKey(s.school(), s.grade(), s.classNum(), s.semester(), s.subject()))
                .forEach((k, list) -> result.add(
                        summaryRow(k.school(), k.grade(), k.classNum(), k.semester(), k.subject(), list, at)));
        groupBy(scores, s -> new GradeKey(s.school(), s.grade(), s.semester(), s.subject()))
                .forEach((k, list) -> result.add(
                        summaryRow(k.school(), k.grade(), null, k.semester(), k.subject(), list, at)));
        return result;
    }

    private OlapClassSubjectSummary summaryRow(School school, int grade, Integer classNum, String semester,
                                               Subject subject, List<StudentSubjectScore> list, LocalDateTime at) {
        var stats = list.stream().mapToDouble(StudentSubjectScore::score).summaryStatistics();
        return OlapClassSubjectSummary.of(school, grade, classNum, semester, subject,
                round1(stats.getAverage()), (int) Math.round(stats.getMax()), (int) Math.round(stats.getMin()),
                list.size(), distributionOf(list), at);
    }

    private GradeDistribution distributionOf(List<StudentSubjectScore> list) {
        Map<GradeLevel, Long> counts = list.stream()
                .collect(Collectors.groupingBy(s -> GradeLevel.from((int) Math.round(s.score())), Collectors.counting()));
        return GradeDistribution.of(
                count(counts, GradeLevel.A), count(counts, GradeLevel.B), count(counts, GradeLevel.C),
                count(counts, GradeLevel.D), count(counts, GradeLevel.F));
    }

    // ---------- 3. 학생 석차 (과목별 + 종합, 반 + 전교) ----------

    private List<OlapStudentRank> buildStudentRanks(List<StudentSubjectScore> scores, LocalDateTime at) {
        List<OlapStudentRank> result = new ArrayList<>();
        addSubjectRanks(result, scores, at);
        addOverallRanks(result, scores, at);
        return result;
    }

    private void addSubjectRanks(List<OlapStudentRank> result, List<StudentSubjectScore> scores, LocalDateTime at) {
        groupBy(scores, s -> new ClassKey(s.school(), s.grade(), s.classNum(), s.semester(), s.subject()))
                .forEach((k, list) -> result.addAll(
                        rankRows(toRankInputs(list), k.semester(), k.subject().name(), AnalyticsScope.CLASS, at)));
        groupBy(scores, s -> new GradeKey(s.school(), s.grade(), s.semester(), s.subject()))
                .forEach((k, list) -> result.addAll(
                        rankRows(toRankInputs(list), k.semester(), k.subject().name(), AnalyticsScope.GRADE, at)));
    }

    private void addOverallRanks(List<OlapStudentRank> result, List<StudentSubjectScore> scores, LocalDateTime at) {
        List<StudentOverall> overalls = toStudentOveralls(scores);
        groupBy(overalls, o -> new ClassOverallKey(o.school(), o.grade(), o.classNum(), o.semester()))
                .forEach((k, list) -> result.addAll(
                        rankRows(toOverallInputs(list), k.semester(), OlapStudentRank.OVERALL, AnalyticsScope.CLASS, at)));
        groupBy(overalls, o -> new GradeOverallKey(o.school(), o.grade(), o.semester()))
                .forEach((k, list) -> result.addAll(
                        rankRows(toOverallInputs(list), k.semester(), OlapStudentRank.OVERALL, AnalyticsScope.GRADE, at)));
    }

    private List<StudentOverall> toStudentOveralls(List<StudentSubjectScore> scores) {
        Map<OverallKey, List<Double>> grouped = new LinkedHashMap<>();
        for (StudentSubjectScore s : scores) {
            OverallKey key = new OverallKey(s.studentId(), s.school(), s.grade(), s.classNum(), s.semester());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s.score());
        }
        return grouped.entrySet().stream()
                .map(e -> new StudentOverall(e.getKey(), averageDouble(e.getValue())))
                .toList();
    }

    private List<OlapStudentRank> rankRows(List<RankInput> inputs, String semester, String subject,
                                           AnalyticsScope scope, LocalDateTime at) {
        List<RankInput> sorted = inputs.stream()
                .sorted(Comparator.comparingDouble(RankInput::score).reversed())
                .toList();
        int total = sorted.size();
        List<OlapStudentRank> rows = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int rank = competitionRank(sorted, i);
            double percentile = round1((1 - (double) rank / total) * 100);
            rows.add(OlapStudentRank.of(sorted.get(i).studentId(), semester, subject, scope,
                    rank, total, percentile, round1(sorted.get(i).score()), at));
        }
        return rows;
    }

    private int competitionRank(List<RankInput> sortedDesc, int index) {
        double score = sortedDesc.get(index).score();
        int higher = 0;
        for (int j = 0; j < index; j++) {
            if (sortedDesc.get(j).score() > score) {
                higher++;
            }
        }
        return higher + 1;   // 동점은 같은 순위, 다음은 건너뜀 (1,2,2,4)
    }

    // ---------- 공통 유틸 ----------

    private <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyFn) {
        return list.stream().collect(Collectors.groupingBy(keyFn, LinkedHashMap::new, Collectors.toList()));
    }

    private List<RankInput> toRankInputs(List<StudentSubjectScore> list) {
        return list.stream().map(s -> new RankInput(s.studentId(), s.score())).toList();
    }

    private List<RankInput> toOverallInputs(List<StudentOverall> list) {
        return list.stream().map(o -> new RankInput(o.studentId(), o.score())).toList();
    }

    private int count(Map<GradeLevel, Long> counts, GradeLevel level) {
        return counts.getOrDefault(level, 0L).intValue();
    }

    private double averageInt(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private double averageDouble(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // ---------- 내부 값 객체 ----------

    private record ScoreKey(Long studentId, School school, int grade, int classNum, String semester, Subject subject) {}

    private record StudentSubjectScore(ScoreKey key, double score) {
        Long studentId() { return key.studentId(); }
        School school() { return key.school(); }
        int grade() { return key.grade(); }
        int classNum() { return key.classNum(); }
        String semester() { return key.semester(); }
        Subject subject() { return key.subject(); }
    }

    private record OverallKey(Long studentId, School school, int grade, int classNum, String semester) {}

    private record StudentOverall(OverallKey key, double score) {
        Long studentId() { return key.studentId(); }
        School school() { return key.school(); }
        int grade() { return key.grade(); }
        int classNum() { return key.classNum(); }
        String semester() { return key.semester(); }
    }

    private record ClassKey(School school, int grade, Integer classNum, String semester, Subject subject) {}

    private record GradeKey(School school, int grade, String semester, Subject subject) {}

    private record ClassOverallKey(School school, int grade, Integer classNum, String semester) {}

    private record GradeOverallKey(School school, int grade, String semester) {}

    private record RankInput(Long studentId, double score) {}
}
