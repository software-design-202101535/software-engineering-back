package com.example.EduManager.domain.student.dto;

import com.example.EduManager.domain.student.entity.StudentNote;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class NoteResponse {

    private Long id;
    private Long studentId;
    private Long teacherId;
    private String category;
    private String content;
    private LocalDate date;
    private LocalDateTime createdAt;

    public static NoteResponse ofForTest(Long id) {
        NoteResponse response = new NoteResponse();
        response.id = id;
        return response;
    }

    public static NoteResponse of(StudentNote note) {
        NoteResponse response = new NoteResponse();
        response.id = note.getId();
        response.studentId = note.getStudent().getId();
        response.teacherId = note.getTeacher().getUser().getId();
        response.category = note.getCategory().name();
        response.content = note.getContent();
        response.date = note.getDate();
        response.createdAt = note.getCreatedAt();
        return response;
    }
}
