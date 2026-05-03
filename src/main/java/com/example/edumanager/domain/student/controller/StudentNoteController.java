package com.example.edumanager.domain.student.controller;

import com.example.edumanager.domain.student.dto.CreateNoteRequest;
import com.example.edumanager.domain.student.dto.NoteResponse;
import com.example.edumanager.domain.student.dto.UpdateNoteRequest;
import com.example.edumanager.domain.student.entity.NoteCategory;
import com.example.edumanager.facade.StudentNoteOperationFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.StudentNoteApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/notes")
@RequiredArgsConstructor
public class StudentNoteController implements StudentNoteApiSpecification {

    private final StudentNoteOperationFacade studentNoteOperationFacade;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam(required = false) NoteCategory category,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(studentNoteOperationFacade.getList(studentId, category, userDetails));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateNoteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentNoteOperationFacade.create(studentId, request, userDetails));
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long noteId,
            @RequestBody @Valid UpdateNoteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(studentNoteOperationFacade.update(studentId, noteId, request, userDetails));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        studentNoteOperationFacade.delete(studentId, noteId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
