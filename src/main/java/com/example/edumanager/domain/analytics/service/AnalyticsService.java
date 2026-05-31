package com.example.edumanager.domain.analytics.service;

import com.example.edumanager.domain.analytics.dto.ClassSummaryResponse;
import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import com.example.edumanager.domain.analytics.repository.OlapClassSubjectSummaryRepository;
import com.example.edumanager.domain.analytics.repository.OlapStudentRankRepository;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OlapClassSubjectSummaryRepository classSummaryRepository;
    private final OlapStudentRankRepository studentRankRepository;

    public ClassSummaryResponse getClassSummary(School school, int grade, Integer classNum,
                                                String semester, Subject subject) {
        List<OlapClassSubjectSummary> rows =
                classSummaryRepository.findForDashboard(school, grade, classNum, semester, subject);
        return ClassSummaryResponse.of(school, grade, classNum, semester, rows);
    }

    public StudentRankResponse getStudentRanks(Long studentId, String semester) {
        List<OlapStudentRank> rows = studentRankRepository.findByStudentIdAndSemester(studentId, semester);
        return StudentRankResponse.of(studentId, semester, rows);
    }
}
