package com.example.edumanager.domain.analytics.entity;

import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 반/학년 × 과목 × 학기 성적 집계 (OLAP). ETL 배치가 채운다.
 * {@code classNum == null} 이면 학년 전체 집계 행이다.
 */
@Entity
@Table(name = "olap_class_subject_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_olap_class_subject",
                columnNames = {"school", "grade", "class_num", "semester", "subject"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OlapClassSubjectSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private School school;

    @Column(nullable = false)
    private int grade;

    @Column(name = "class_num")
    private Integer classNum;   // null = 학년 전체

    @Column(nullable = false, length = 10)
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Subject subject;

    @Column(name = "avg_score", nullable = false)
    private double avgScore;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "min_score", nullable = false)
    private int minScore;

    @Column(name = "student_count", nullable = false)
    private int studentCount;

    @Embedded
    private GradeDistribution distribution;

    @Column(name = "aggregated_at", nullable = false)
    private LocalDateTime aggregatedAt;

    private OlapClassSubjectSummary(School school, int grade, Integer classNum, String semester, Subject subject,
                                    double avgScore, int maxScore, int minScore, int studentCount,
                                    GradeDistribution distribution, LocalDateTime aggregatedAt) {
        this.school = school;
        this.grade = grade;
        this.classNum = classNum;
        this.semester = semester;
        this.subject = subject;
        this.avgScore = avgScore;
        this.maxScore = maxScore;
        this.minScore = minScore;
        this.studentCount = studentCount;
        this.distribution = distribution;
        this.aggregatedAt = aggregatedAt;
    }

    public static OlapClassSubjectSummary of(School school, int grade, Integer classNum, String semester, Subject subject,
                                             double avgScore, int maxScore, int minScore, int studentCount,
                                             GradeDistribution distribution, LocalDateTime aggregatedAt) {
        return new OlapClassSubjectSummary(school, grade, classNum, semester, subject,
                avgScore, maxScore, minScore, studentCount, distribution, aggregatedAt);
    }

    public boolean isGradeWide() {
        return classNum == null;
    }
}
