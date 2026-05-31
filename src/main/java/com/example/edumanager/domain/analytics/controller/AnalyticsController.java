package com.example.edumanager.domain.analytics.controller;

import com.example.edumanager.domain.analytics.dto.ClassSummaryResponse;
import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.service.AnalyticsService;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.facade.AnalyticsRankFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.AnalyticsApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController implements AnalyticsApiSpecification {

    private final AnalyticsService analyticsService;
    private final AnalyticsRankFacade analyticsRankFacade;

    @Override
    @GetMapping("/class-summary")
    public ResponseEntity<ClassSummaryResponse> getClassSummary(
            @RequestParam School school,
            @RequestParam int grade,
            @RequestParam(required = false) Integer classNum,
            @RequestParam String semester,
            @RequestParam(required = false) Subject subject) {
        return ResponseEntity.ok(analyticsService.getClassSummary(school, grade, classNum, semester, subject));
    }

    @Override
    @GetMapping("/students/{studentId}/ranks")
    public ResponseEntity<StudentRankResponse> getStudentRanks(
            @PathVariable Long studentId,
            @RequestParam String semester,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(analyticsRankFacade.getStudentRanks(studentId, semester, userDetails));
    }
}
