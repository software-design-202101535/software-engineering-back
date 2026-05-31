package com.example.edumanager.domain.analytics.dto;

import com.example.edumanager.domain.analytics.entity.AnalyticsScope;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.grade.entity.GradeLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudentRankResponse {

    private final Long studentId;
    private final String semester;
    private final LocalDateTime updatedAt;

    @JsonProperty("class")
    private final ScopeRanks classRanks;   // 반 석차 (집계 없으면 null)

    @JsonProperty("grade")
    private final ScopeRanks gradeRanks;    // 전교(학년) 석차

    private StudentRankResponse(Long studentId, String semester, LocalDateTime updatedAt,
                                ScopeRanks classRanks, ScopeRanks gradeRanks) {
        this.studentId = studentId;
        this.semester = semester;
        this.updatedAt = updatedAt;
        this.classRanks = classRanks;
        this.gradeRanks = gradeRanks;
    }

    public static StudentRankResponse of(Long studentId, String semester, List<OlapStudentRank> rows) {
        LocalDateTime updatedAt = rows.isEmpty() ? null : rows.get(0).getAggregatedAt();
        return new StudentRankResponse(studentId, semester, updatedAt,
                ScopeRanks.of(rows, AnalyticsScope.CLASS), ScopeRanks.of(rows, AnalyticsScope.GRADE));
    }

    @Getter
    public static class ScopeRanks {
        private final OverallRank overall;          // 종합 석차 (없으면 null)
        private final List<SubjectRank> subjects;   // 과목별 석차

        private ScopeRanks(OverallRank overall, List<SubjectRank> subjects) {
            this.overall = overall;
            this.subjects = subjects;
        }

        private static ScopeRanks of(List<OlapStudentRank> rows, AnalyticsScope scope) {
            List<OlapStudentRank> scoped = rows.stream().filter(r -> r.getScope() == scope).toList();
            if (scoped.isEmpty()) {
                return null;
            }
            OverallRank overall = scoped.stream()
                    .filter(r -> r.getSubject().equals(OlapStudentRank.OVERALL))
                    .findFirst().map(OverallRank::of).orElse(null);
            List<SubjectRank> subjects = scoped.stream()
                    .filter(r -> !r.getSubject().equals(OlapStudentRank.OVERALL))
                    .map(SubjectRank::of).toList();
            return new ScopeRanks(overall, subjects);
        }
    }

    @Getter
    public static class OverallRank {
        private final int rank;
        private final int totalCount;
        private final double percentile;
        private final double avgScore;

        private OverallRank(int rank, int totalCount, double percentile, double avgScore) {
            this.rank = rank;
            this.totalCount = totalCount;
            this.percentile = percentile;
            this.avgScore = avgScore;
        }

        private static OverallRank of(OlapStudentRank row) {
            return new OverallRank(row.getRankPosition(), row.getTotalCount(), row.getPercentile(), row.getAvgScore());
        }
    }

    @Getter
    public static class SubjectRank {
        private final String subject;
        private final int rank;
        private final int totalCount;
        private final double percentile;
        private final double avgScore;
        private final String gradeLevel;

        private SubjectRank(String subject, int rank, int totalCount, double percentile,
                            double avgScore, String gradeLevel) {
            this.subject = subject;
            this.rank = rank;
            this.totalCount = totalCount;
            this.percentile = percentile;
            this.avgScore = avgScore;
            this.gradeLevel = gradeLevel;
        }

        private static SubjectRank of(OlapStudentRank row) {
            String gradeLevel = GradeLevel.from((int) Math.round(row.getAvgScore())).name();
            return new SubjectRank(row.getSubject(), row.getRankPosition(), row.getTotalCount(),
                    row.getPercentile(), row.getAvgScore(), gradeLevel);
        }
    }
}
