package com.example.edumanager.domain.student.dto;

import com.example.edumanager.domain.student.entity.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateNoteRequest {

    @NotNull(message = "카테고리를 입력해주세요.")
    private NoteCategory category;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;

    public static CreateNoteRequest of(NoteCategory category, String content, LocalDate date) {
        CreateNoteRequest request = new CreateNoteRequest();
        request.category = category;
        request.content = content;
        request.date = date;
        return request;
    }
}
