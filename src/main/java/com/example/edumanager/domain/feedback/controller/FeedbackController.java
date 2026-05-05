package com.example.edumanager.domain.feedback.controller;

import com.example.edumanager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.FeedbackResponse;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import com.example.edumanager.facade.FeedbackOperationFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.FeedbackApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/feedbacks")
@RequiredArgsConstructor
public class FeedbackController implements FeedbackApiSpecification {

    private final FeedbackOperationFacade feedbackOperationFacade;

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam(required = false) FeedbackCategory category,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(feedbackOperationFacade.getList(studentId, category, userDetails));
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateFeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackOperationFacade.create(studentId, request, userDetails));
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @RequestBody @Valid UpdateFeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(feedbackOperationFacade.update(studentId, feedbackId, request, userDetails));
    }

    @PatchMapping("/{feedbackId}/visibility")
    public ResponseEntity<FeedbackResponse> updateVisibility(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @RequestBody @Valid UpdateFeedbackVisibilityRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(feedbackOperationFacade.updateVisibility(studentId, feedbackId, request, userDetails));
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        feedbackOperationFacade.delete(studentId, feedbackId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
