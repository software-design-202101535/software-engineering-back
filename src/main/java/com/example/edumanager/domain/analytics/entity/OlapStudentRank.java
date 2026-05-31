package com.example.edumanager.domain.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학생 × (과목 또는 {@code "OVERALL"}) × scope × 학기 석차 (OLAP). ETL 배치가 채운다.
 * 전 학생을 정렬해야 나오는 값(석차/백분위)만 담는다. OLTP 로는 매번 계산하기 무겁다.
 */
@Entity
@Table(name = "olap_student_rank",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_olap_student_rank",
                columnNames = {"student_id", "semester", "subject", "scope"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OlapStudentRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 10)
    private String semester;

    @Column(nullable = false, length = 30)
    private String subject;   // 과목 enum 이름 또는 "OVERALL"(종합)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AnalyticsScope scope;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;   // rank 는 MySQL 8 예약어

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private double percentile;

    @Column(name = "avg_score", nullable = false)
    private double avgScore;

    @Column(name = "aggregated_at", nullable = false)
    private LocalDateTime aggregatedAt;

    private OlapStudentRank(Long studentId, String semester, String subject, AnalyticsScope scope,
                           int rankPosition, int totalCount, double percentile, double avgScore,
                           LocalDateTime aggregatedAt) {
        this.studentId = studentId;
        this.semester = semester;
        this.subject = subject;
        this.scope = scope;
        this.rankPosition = rankPosition;
        this.totalCount = totalCount;
        this.percentile = percentile;
        this.avgScore = avgScore;
        this.aggregatedAt = aggregatedAt;
    }

    public static OlapStudentRank of(Long studentId, String semester, String subject, AnalyticsScope scope,
                                     int rankPosition, int totalCount, double percentile, double avgScore,
                                     LocalDateTime aggregatedAt) {
        return new OlapStudentRank(studentId, semester, subject, scope,
                rankPosition, totalCount, percentile, avgScore, aggregatedAt);
    }

    public static final String OVERALL = "OVERALL";
}
