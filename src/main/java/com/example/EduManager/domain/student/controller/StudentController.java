package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.StudentDetailResponse;
import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
import com.example.EduManager.facade.StudentOperationFacade;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.swagger.StudentApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController implements StudentApiSpecification {

    private final StudentOperationFacade studentOperationFacade;

    @GetMapping
    public ResponseEntity<List<StudentSummaryResponse>> getClassStudents(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(studentOperationFacade.getClassStudents(userDetails));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDetailResponse> getStudentDetail(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(studentOperationFacade.getStudentDetail(studentId, userDetails));
    }

    @PatchMapping("/{studentId}")
    public ResponseEntity<StudentDetailResponse> updateStudentDetail(
            @PathVariable Long studentId,
            @RequestBody @Valid UpdateStudentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(studentOperationFacade.updateStudentDetail(studentId, request, userDetails));
    }
}
