package com.example.edumanager.domain.grade.controller;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.dto.GradeResponse;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.facade.GradeOperationFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.GradeApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/grades")
@RequiredArgsConstructor
public class GradeController implements GradeApiSpecification {

    private final GradeOperationFacade gradeOperationFacade;

    @GetMapping
    public ResponseEntity<List<GradeResponse>> getGrades(
            @PathVariable Long studentId,
            @RequestParam(required = true) String semester,
            @RequestParam(required = true) ExamType examType,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(gradeOperationFacade.getGrades(studentId, userDetails, semester, examType));
    }

    @PutMapping("/batch")
    public ResponseEntity<List<GradeResponse>> batchProcess(
            @PathVariable Long studentId,
            @Valid @RequestBody BatchGradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(gradeOperationFacade.batchProcess(studentId, userDetails, request));
    }
}
