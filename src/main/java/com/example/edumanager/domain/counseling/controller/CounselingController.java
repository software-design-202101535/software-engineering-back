package com.example.edumanager.domain.counseling.controller;

import com.example.edumanager.domain.counseling.dto.CounselingResponse;
import com.example.edumanager.domain.counseling.dto.CreateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.edumanager.facade.CounselingOperationFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.CounselingApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/counselings")
@RequiredArgsConstructor
public class CounselingController implements CounselingApiSpecification {

    private final CounselingOperationFacade counselingOperationFacade;

    @GetMapping
    public ResponseEntity<List<CounselingResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(counselingOperationFacade.getList(studentId, year, month, userDetails));
    }

    @PostMapping
    public ResponseEntity<CounselingResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateCounselingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(counselingOperationFacade.create(studentId, request, userDetails));
    }

    @PutMapping("/{counselingId}")
    public ResponseEntity<CounselingResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @RequestBody @Valid UpdateCounselingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(counselingOperationFacade.update(studentId, counselingId, request, userDetails));
    }

    @PatchMapping("/{counselingId}/share")
    public ResponseEntity<CounselingResponse> updateShare(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @RequestBody @Valid UpdateCounselingShareRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(counselingOperationFacade.updateShare(studentId, counselingId, request, userDetails));
    }

    @DeleteMapping("/{counselingId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        counselingOperationFacade.delete(studentId, counselingId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
