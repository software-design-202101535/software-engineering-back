package com.example.EduManager.domain.student.controller;

import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.facade.StudentOperationFacade;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.swagger.StudentApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
