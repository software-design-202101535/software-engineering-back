package com.example.edumanager.domain.analytics.dto;

import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClassSummaryResponse {

    private final School school;
    private final int grade;
    private final Integer classNum;   // null = 학년 전체
    private final String semester;
    private final LocalDateTime updatedAt;
    private final List<SubjectSummary> subjects;

    private ClassSummaryResponse(School school, int grade, Integer classNum, String semester,
                                 LocalDateTime updatedAt, List<SubjectSummary> subjects) {
        this.school = school;
        this.grade = grade;
        this.classNum = classNum;
        this.semester = semester;
        this.updatedAt = updatedAt;
        this.subjects = subjects;
    }

    public static ClassSummaryResponse of(School school, int grade, Integer classNum, String semester,
                                          List<OlapClassSubjectSummary> rows) {
        LocalDateTime updatedAt = rows.isEmpty() ? null : rows.get(0).getAggregatedAt();
        List<SubjectSummary> subjects = rows.stream().map(SubjectSummary::of).toList();
        return new ClassSummaryResponse(school, grade, classNum, semester, updatedAt, subjects);
    }

    @Getter
    public static class SubjectSummary {
        private final Subject subject;
        private final double avgScore;
        private final int maxScore;
        private final int minScore;
        private final int studentCount;
        private final Map<String, Integer> distribution;

        private SubjectSummary(Subject subject, double avgScore, int maxScore, int minScore,
                               int studentCount, Map<String, Integer> distribution) {
            this.subject = subject;
            this.avgScore = avgScore;
            this.maxScore = maxScore;
            this.minScore = minScore;
            this.studentCount = studentCount;
            this.distribution = distribution;
        }

        private static SubjectSummary of(OlapClassSubjectSummary row) {
            Map<String, Integer> distribution = new LinkedHashMap<>();
            distribution.put("A", row.getDistribution().getA());
            distribution.put("B", row.getDistribution().getB());
            distribution.put("C", row.getDistribution().getC());
            distribution.put("D", row.getDistribution().getD());
            distribution.put("F", row.getDistribution().getF());
            return new SubjectSummary(row.getSubject(), row.getAvgScore(), row.getMaxScore(),
                    row.getMinScore(), row.getStudentCount(), distribution);
        }
    }
}
