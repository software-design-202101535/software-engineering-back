package com.example.EduManager.domain.attendance.controller;

import com.example.EduManager.domain.attendance.dto.AttendanceResponse;
import com.example.EduManager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.EduManager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.EduManager.facade.AttendanceOperationFacade;
import com.example.EduManager.global.security.UserDetailsImpl;
import com.example.EduManager.global.swagger.AttendanceApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/attendance")
@RequiredArgsConstructor
public class AttendanceController implements AttendanceApiSpecification {

    private final AttendanceOperationFacade attendanceOperationFacade;

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(attendanceOperationFacade.getList(studentId, year, month, userDetails));
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateAttendanceRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceOperationFacade.create(studentId, request, userDetails));
    }

    @PatchMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long attendanceId,
            @RequestBody @Valid UpdateAttendanceRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(attendanceOperationFacade.update(studentId, attendanceId, request, userDetails));
    }

    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long attendanceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        attendanceOperationFacade.delete(studentId, attendanceId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
